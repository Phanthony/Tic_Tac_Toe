import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

// this is where you will begin the first version of your project
// but you will also need to connect this project with your github.uiowa.edu
// repository, as explained in the discussion section 9th November

class InnerTTT(private val setRows:Int=3, private val setColumns:Int=3, private val toWin:Int=3, private val playerNum:Int=2): TicTacToe{
    private val gameBoard = mutableListOf<MutableList<Int>>()
    private var currentPlayer = 1
    private var moves: Int = 0
    private var gameState = 0 //0 - ongoing, 1 - player 1 wins, 2 - player 2 wins .. playerNum+1 - game ended in draw
    private var totalPlayers = playerNum


    //Initializes the game
    override fun createGame() {
        if ((toWin > setRows && toWin > setColumns) || (setColumns == 0) || (setRows == 0) || (toWin == 0)){
            throw ErrorMessage("Can not create game")
        }
        else {
            gameBoard.clear()
            moves = setRows * setColumns
            for (row in 1..setRows){
                gameBoard.add(MutableList(setColumns){0})
            }
            printGame()
        }
    }

    // Places a game piece on board
    override fun place(row: Int, column: Int) {
        if ((row > setRows || column > setColumns) || (row == 0 || column == 0) || (gameBoard[row-1][column-1] != 0)){
            printGame()
            throw ErrorMessage("Can not place game piece")
        }
        else {
            println("\nPlayer $currentPlayer places their game piece at row $row, column $column\n")
            gameBoard[row - 1][column - 1] = currentPlayer
            moves -= 1
            printGame()
            if (check(row - 1, column - 1)) {
                println("\nPlayer $currentPlayer has won the game")
                gameState = currentPlayer
                //createGame()
            } else{
                if (moves == 0) {
                    println("\n Game ended in a draw")
                    gameState = playerNum+1
                } else {
                    switchPlayer()
                }
            }
        }
    }

    // Switches Player
    private fun switchPlayer(){
        currentPlayer = when(currentPlayer){
            playerNum -> 1
            else -> currentPlayer+1
        }
    }

    // Efficient win check based on last game piece placed
    override fun check(row:Int, column: Int): Boolean {
        var counter = 0
        for (i in (max(0,column-toWin+1))..min(setColumns-1,column+toWin-1)){ // check horizontal
            // print(Pair(row,i))
            when(gameBoard[row][i]){
                currentPlayer -> counter++
                else -> counter = 0
            }
            if (counter == toWin){
                return true
            }
        }
        counter = 0
        for (i in (max(0,row-toWin+1))..min(setRows-1,row+toWin-1)){ // check vertical
            // print(Pair(i,column))
            when(gameBoard[i][column]){
                currentPlayer -> counter++
                else -> counter = 0
            }
            if (counter == toWin){
                return true
            }
        }
        counter=0
        var temp = min(min(abs(0-row), toWin-1), min(abs(0-column),toWin-1))
        var rowTemp = row-temp
        var columnTemp = column-temp

        // check diagonal going right
        while ((rowTemp < setRows && columnTemp < setColumns) && (rowTemp < row+toWin && columnTemp < column+toWin)){
            //println(Pair(rowTemp,columnTemp))
            when(gameBoard[rowTemp][columnTemp]) {
                currentPlayer -> counter++
                else -> counter = 0
            }
            if (counter == toWin){
                return true
            }
            rowTemp+=1
            columnTemp+=1
        }

        counter = 0
        // check diagonal going left
        temp = min(abs(0-row), abs(setColumns-column-1))
        rowTemp = row-temp
        columnTemp = column+temp
        while (rowTemp < setRows && columnTemp >= 0){
            //println(Pair(rowTemp,columnTemp))
            when(gameBoard[rowTemp][columnTemp]) {
                currentPlayer -> counter++
                else -> counter = 0
            }
            if (counter == toWin){
                return true
            }
            rowTemp+=1
            columnTemp-=1
        }
        return false
    }

    // prints the game out for debugging purposes
    override fun printGame() {
        val maxSpace = max((setColumns+1).toString().length,(setRows+1).toString().length)
        val temp = IntArray(setColumns+1){it}.toList()
        val bufferCol = " ".repeat(maxSpace)
        val topGrid = arrayListOf<String>(bufferCol)
        for (i in 1..temp.size-1){
            when {
                maxSpace - i.toString().length > 0 -> topGrid.add(" ".repeat(maxSpace - i.toString().length) + temp[i])
                else -> topGrid.add(temp[i].toString())
            }
        }
        println(topGrid)
        var counter = 1
        for (rows in gameBoard){
            val eachRow = arrayListOf<String>()
            when {
                maxSpace - counter.toString().length > 0 -> eachRow.add(" ".repeat(maxSpace - counter.toString().length) + counter)
                else -> eachRow.add(counter.toString())
            }
            for (i in rows){
                when(i){
                    3 -> eachRow.add(" ".repeat(maxSpace-1) + "O")
                    1 -> eachRow.add(" ".repeat(maxSpace-1) + "X")
                    2 -> eachRow.add(" ".repeat(maxSpace-1) + "T")
                    else -> eachRow.add(" ".repeat(maxSpace-1) + " ")
                }
            }
            println(eachRow)
            counter++
        }
    }

    // Undo the last move
    override fun undoMove(row: Int,column: Int){
        gameBoard[row][column] = 0
        currentPlayer = when(currentPlayer){
            1 -> totalPlayers
            else -> currentPlayer-1
        }
        moves++
        printGame()
        println(currentPlayer)
    }

    // Set of functions to safely return information

    override fun getPlayer(): Int{
        return currentPlayer
    }
    override fun getState(): Int{
        return gameState
    }
    override fun getBoard(): MutableList<MutableList<Int>>{
        return gameBoard
    }
    override fun getDimensions(): Triple<Int,Int,Int>{
        return Triple(setRows,setColumns,toWin)
    }


}

/*
fun main(args: Array<String>) {
val game1 = InnerTTT()
game1.createGame()
game1.printGame()
game1.place(1,1)
game1.place(1,2)
game1.place(2,2)
game1.place(3,3)
game1.place(2,3)
game1.place(2,1)
game1.place(1,3)
game1.place(3,1)
game1.place(3,2)

val game2 = InnerTTT()
game2.createGame()
game2.place(1,3)
game2.place(1,2)
game2.place(2,2)
game2.place(1,1)
game2.place(3,1)

print("\nEnter two numbers for an NxM board: ")
val temp = readLine()!!.split(Regex("\\s+")).filter { it != "" }
print("\nEnter number of pieces needed in a row to win: ")
val tempWin = readLine()!!.toInt()
val game3 = InnerTTT(temp[0].toInt(),temp[1].toInt(),tempWin)
game3.createGame()
while (game3.getState() == 0){
    print("\nPlayer ${game3.getPlayer()}'s turn")
    print("\nEnter two numbers to place at NxM: ")
    val rowcol = readLine()!!.split(Regex("\\s+")).filter { it != "" }
    game3.place(rowcol[0].toInt(), rowcol[1].toInt())
}
*/

