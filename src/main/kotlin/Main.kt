package chess

enum class InitStep (val steps: List<String>) {
    P1InitStep( listOf("a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2") ),
    P2InitStep( listOf("a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7") )

//    P1InitStep( listOf("d6", "b2", "c2", "d2", "e2", "f2", "g2", "h2") ),
//    P2InitStep( listOf("e7", "f7", "g7", "h7", "a5") )
}

fun printBroad(chessList: MutableList<MutableList<String>>):Unit {
    val lineA = "  +---+---+---+---+---+---+---+---+"
    for(numChar in '8' downTo '1'){
        var row = "$numChar |"
        for(char in 'a'..'h'){
            var p = " "
            val pattern = "$char$numChar"
            if(chessList[0].contains(pattern)){
                p = "W"
            }
            if(chessList[1].contains(pattern)){
                p = "B"
            }
            row += " $p |"
        }
        println(lineA)
        println(row)
    }

    println(lineA)
    println("    a   b   c   d   e   f   g   h")
}

fun checkWhoseTurn(move: Int):Int{
    return move%2
}

fun isInputStringValid(str: String): Boolean{
    val pattern = Regex("[a-h][1-8][a-h][1-8]")
    return str.matches(pattern)
}

fun getRivalPlayer(p: Int): Int{
    return if(p == 0) 1 else 0
}

fun checkIsInitStep (currentPositionStr: String, currentPlayer: Int): Boolean {

    val p1InitStep = InitStep.P1InitStep.steps
    val p2InitStep = InitStep.P2InitStep.steps

    return when (currentPlayer) {
        0 -> p1InitStep.contains(currentPositionStr)
        else -> p2InitStep.contains(currentPositionStr)
    }
}

fun getVerticalForwardPosition(currentPositionStr: String, currentPlayer: Int, steps: Int): String{
    return when (currentPlayer) {
        0 -> "${currentPositionStr[0]}${currentPositionStr[1]+steps}"
        else ->  "${currentPositionStr[0]}${currentPositionStr[1]-steps}"
    }
}

fun getPresentDiagonalPositions (player: Int, str: String): List<String>{
    return when(player) {
        0 -> {
            val dl = "${str[0]+1}${str[1]+1}"
            val d2 = "${str[0]-1}${str[1]+1}"
            listOf(dl, d2)
        }
        else -> {
            val dl = "${str[0]+1}${str[1]-1}"
            val d2 = "${str[0]-1}${str[1]-1}"
            listOf(dl, d2)
        }
    }
}


fun getCapturedRivalChessFromDiagonal (chessList: List<List<String>>, player: Int, rival: Int, str: String) : String? {
    // check if captured rival chess at diagonal
    val presentDiagonalPositions = getPresentDiagonalPositions(player, str)

    var capturedRivalChessFromDiagonal: String? = null
    for (ele in 0..1){
        val d = presentDiagonalPositions[ele]
        if(
            chessList[rival].contains(d) &&
            "${str[2]}${str[3]}" == d
        ){
            capturedRivalChessFromDiagonal = d
        }
    }

    return capturedRivalChessFromDiagonal
}


fun main() {
//    write your code here
//    var isExit = false
    var move = 0

    val p1InitStep = InitStep.P1InitStep.steps
    val p2InitStep = InitStep.P2InitStep.steps

    val p1ChessList = p1InitStep.toMutableList()
    val p2ChessList = p2InitStep.toMutableList()
    val chessList: MutableList<MutableList<String>> = mutableListOf(p1ChessList, p2ChessList)

    println("Pawns-Only Chess")
    println("First Player's name:")
    print(">")
    val p1 = readln()
    println("Second Player's name:")
    print(">")
    val p2 = readln()

    // print initial chess board
    printBroad(chessList)

    var enPassant :String? = null
    Loop@while(true){
        val player = checkWhoseTurn(move)
        val rival = getRivalPlayer(player)

        println("${if(player == 0) p1 else p2}'s turn:")

        print(">")
        val str = readln()

        if(str == "exit") {
            break
        }

        // check if there is a responding chess in the starting position
        val matchedChessIndex =  chessList[player].indexOf(str.substring(0,2))
        if(matchedChessIndex < 0){
            if(player == 0){
                println("No white pawn at ${str.substring(0,2)}")
            } else {
                println("No black pawn at ${str.substring(0,2)}")
            }
            continue
        }


        var capturedRivalChessFromDiagonal: String? = getCapturedRivalChessFromDiagonal(chessList, player, rival, str)


        // check if capture rival by en Passant
        var capturedRivalChessEnPassant: String? = null
        for (ele in 0..1){
            val d = getPresentDiagonalPositions(player, str)[ele]
            if(
                enPassant != null && d == enPassant.substring(3,5)
            ){
                capturedRivalChessEnPassant = enPassant.substring(0,2)
            }
        }



        val isInitStep = checkIsInitStep(str.substring(0,2), player)
        val forwardStepDiff = if( player==0 ) str[3] - str[1] else str[1] - str[3]

        val isStepOnRivalChess = if( player==0 )
            chessList[1].contains(str.substring(2,4)) else chessList[0].contains(str.substring(2,4))
        val invalidInputString = !isInputStringValid(str)
        val invalidStepNumber = isInitStep && forwardStepDiff !in 1..2 || !isInitStep && forwardStepDiff != 1
        val invalidMovingVerticalLine = (str[0] != str[2] && capturedRivalChessFromDiagonal == null) && capturedRivalChessEnPassant == null
        val invalidStepOnRivalChess = isStepOnRivalChess && capturedRivalChessFromDiagonal == null


        // validate move
        if(
            invalidInputString ||
            invalidStepNumber ||
            invalidMovingVerticalLine ||
            invalidStepOnRivalChess
        ){
            println("Invalid Input")
            continue@Loop
        }
        // validate move ended

        // assign enPassant for rival's subsequent round
        /**
         * format:
         *     {rival position}:{capture position}
         *     e.g d5d6
         */
        enPassant = when {
            isInitStep && forwardStepDiff == 2 && player == 0 -> "${str[2]}${str[3]}:${str[2]}${str[3]-1}"
            isInitStep && forwardStepDiff == 2 && player == 1 -> "${str[2]}${str[3]}:${str[2]}${str[3]+1}"
            else -> null
        }
        // assign enPassant ended

        // update chess
        if(capturedRivalChessFromDiagonal != null){
            chessList[rival].remove(capturedRivalChessFromDiagonal)
        }

        if(capturedRivalChessEnPassant != null){
            chessList[rival].remove(capturedRivalChessEnPassant)
        }


        chessList[player][matchedChessIndex] = str.substring(2,4)
        printBroad(chessList)


        //Check if Current Player Wins
        if(chessList[rival].size == 0){
            println("${if(player == 0) "White" else "Black"} Wins!")
            break@Loop
        }

        if(player == 0 && str[3] == '8'){
            println("White Wins!")
            break@Loop
        }

        if(player == 1 && str[3] == '1'){
            println("Black Wins!")
            break@Loop
        }

        // checkIfStalemate
        var isStalemate = true
        checkIfStalemateLoops@for (currentPlayerPos in chessList[player]) {
            for (currentRivalPos in chessList[rival]) {
                val rivalForward1StepIsValid =
                    !chessList[player].contains(getVerticalForwardPosition(currentRivalPos, rival, 1))
                val rivalForward2StepIsValid =
                    !chessList[player].contains(getVerticalForwardPosition(currentRivalPos, rival, 2))

                var capturedRivalChessFromDiagonal: String? = getCapturedRivalChessFromDiagonal (
                    chessList, rival, player, "${currentRivalPos}${currentPlayerPos}")

                if(
                    (checkIsInitStep(currentRivalPos, player) && rivalForward1StepIsValid && rivalForward2StepIsValid) ||
                    (!checkIsInitStep(currentRivalPos, player) && rivalForward1StepIsValid) ||
                    (capturedRivalChessFromDiagonal != null)
                ) {
                    isStalemate = false
                    checkIfStalemateLoops@break
                }
            }
        }
        // checkIfStalemate end

        //End the game if Stalemate
        if(isStalemate) {
            println("Stalemate!")
            break@Loop
        }

        move++
    }

    println("Bye!")
}