interface TicTacToe {
    //Creates the game
    fun createGame()
    //Prints the Game, only used for testing
    fun printGame()
    //checks whether the game has been won or not
    fun check(row: Int,column: Int):Boolean
    //places a game piece onto the board
    fun place(row:Int,column:Int)
    //Undo a move
    fun undoMove(row: Int,column: Int)
    // Set of functions to safely return information
    fun getPlayer(): Int
    fun getState(): Int
    fun getBoard(): MutableList<MutableList<Int>>
    fun getDimensions(): Triple<Int,Int,Int>

}

class ErrorMessage(private val reason: String) : Exception("Error: $reason"){
    fun getError(): String{
        return reason
    }
}