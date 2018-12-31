import javafx.application.Application
import javafx.application.Application.launch
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.stage.Stage
import kotlin.math.roundToInt


var colNum = 3
var rowNum = 3
var winNum = 3
var playerHolder = mutableListOf<Players>()
var currentGameGui: Stage? = null
var currentGame: TicTacToe? = null
var currentPlayer: Int = 0
var totalPlayers = 2
val playerFrame = HBox(55.0)
val moveFrame = VBox(5.0)
var coordOfButton = mutableMapOf<Pair<Int,Int>,GameButton>()
var moveList = mutableListOf<Pair<Int,Pair<Int,Int>>>()


// Starting the Gui interface,
// This stage lets users choose either 2-3 players
class NumberPlayers: Application() {
    override fun start(primaryStage: Stage?) {
        primaryStage!!.title = "Game Setup"

        val mainFrame = HBox(10.0)
        mainFrame.padding = Insets(20.0, 20.0, 20.0, 20.0)
        mainFrame.alignment = Pos.CENTER

        val button2 = Button("Two Players")
        button2.prefWidthProperty().bind(mainFrame.widthProperty())
        button2.prefHeightProperty().bind(mainFrame.heightProperty())
        button2.onAction = players2

        val button3 = Button("Three Players")
        button3.prefWidthProperty().bind(mainFrame.widthProperty())
        button3.prefHeightProperty().bind(mainFrame.heightProperty())
        button3.onAction = players3

        mainFrame.apply { children.addAll(button2, button3) }

        primaryStage.run {
            scene = Scene(mainFrame, 330.0,330.0)
            show()
        }
    }
}

// These two event handlers choose the total amount of players that'll play the game
object players3: EventHandler<ActionEvent>{
    //shout out to my roommate ian clay
    override fun handle(event: ActionEvent?) {
        totalPlayers = 3
        (event!!.source as Button).scene.window.hide()
        PickColor()
    }

}
object players2: EventHandler<ActionEvent>{
    override fun handle(event: ActionEvent?) {
        totalPlayers = 2
        (event!!.source as Button).scene.window.hide()
        PickColor()
    }

}

// Gui interface to get user input for their Board and the amount in a row to win
// If everything is left blank it will automatically default to a typical 3x3 board with 3 in a row to win.
class GameCreationStage : Stage(), EventHandler<ActionEvent> {
    private val colEntry = TextField()
    private val rowEntry = TextField()
    private val winEntry = TextField()

    init {
        this.title = "Game Setup"
        val outerPane = FlowPane(10.0, 10.0)
        outerPane.padding = Insets(20.0, 10.0, 20.0, 10.0)
        outerPane.alignment = Pos.CENTER
        val innerPane = TilePane(1.0, 10.0)
        innerPane.prefColumns = 2
        innerPane.prefRows = 3
        val colText = Label("Enter Columns:")
        colText.maxWidth = Double.MAX_VALUE
        colEntry.maxWidth = Double.MAX_VALUE
        innerPane.apply { children.addAll(colText, colEntry) }
        val rowText = Label("Enter Rows:")
        rowText.maxWidth = Double.MAX_VALUE
        rowEntry.maxWidth = Double.MAX_VALUE
        innerPane.apply { children.addAll(rowText, rowEntry) }
        val winText = Label("Enter in a row to win:")
        winText.maxWidth = Double.MAX_VALUE
        winEntry.maxWidth = Double.MAX_VALUE
        innerPane.apply { children.addAll(winText, winEntry) }
        val buttonFrame = TilePane()
        buttonFrame.alignment = Pos.CENTER
        val submitButton = Button("Start")
        submitButton.onAction = this
        submitButton.maxWidth = Double.MAX_VALUE
        buttonFrame.apply { children.add(submitButton) }
        outerPane.apply { children.addAll(innerPane, buttonFrame) }
        this.run {
            this.scene = Scene(outerPane, 340.0, 150.0)
            show()
        }
    }

    override fun handle(event: ActionEvent?) {
        val rowGetter = rowEntry
        val colGetter = colEntry
        val winGetter = winEntry

        if (rowGetter.text != "") rowNum = rowGetter.text.toInt()

        if (colGetter.text != "") colNum = colGetter.text.toInt()

        if (winGetter.text != "") winNum = winGetter.text.toInt()

        try {
            val newGame = InnerTTT(rowNum, colNum, winNum, totalPlayers)
            (event!!.source as Button).scene.window.hide()
            currentGame = newGame
            newGame.createGame()
            currentPlayer = newGame.getPlayer()
            TTTGui(currentGame!!)
        }
        catch (k: ErrorMessage){
            val message = k.getError()
            val tempLabel = Label("Error: $message")
            tempLabel.font = Font.font("Verdana",30.0)
            tempLabel.textFill = Color.RED
            tempLabel.alignment = Pos.CENTER
            val tempButton = Button("Ok")
            tempButton.style = "-fx-padding: 20;"
            //tempButton.onAction = NameGetter
            CreateConditionWindow(tempLabel,tempButton)
        }
    }
}

// The main GUI interface for the game, this class creates the game along with each button and places them in the gui
class TTTGui(currentGame: TicTacToe) : Stage() {
    init {
        currentGameGui = this
        val dimensions = currentGame.getDimensions()
        val rows = dimensions.first
        val columns = dimensions.second
        this.title = "Tic Tac Toe"
        val mainFrame = VBox(7.5)
        mainFrame.style = "-fx-background-color: #000000;"
        mainFrame.alignment = Pos.CENTER
        for (i in 1..rows) {
            val tempFrame = HBox(7.0)
            tempFrame.style = "-fx-background-color: #000000;"
            tempFrame.alignment = Pos.CENTER
            tempFrame.prefHeightProperty().bind(mainFrame.heightProperty())
            for (x in 1..columns) {
                val tempButton = GameButton(i,x)
                tempButton.text = ("$i,$x")
                tempButton.onAction = ButtonPlace
                tempButton.prefWidthProperty().bind(tempFrame.widthProperty())
                tempButton.prefHeightProperty().bind(tempFrame.heightProperty())
                tempFrame.apply { children.add(tempButton) }
            }
            mainFrame.apply { children.add(tempFrame) }

        }
        moveList.clear()
        moveFrame.children.clear()
        val tempScroll = ScrollPane(moveFrame)
        tempScroll.hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
        tempScroll.vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
        val outerGrid = GridPane()
        outerGrid.vgap = 10.0
        outerGrid.hgap = 10.0
        val undoButton = Button("UNDO")
        undoButton.font = Font.font("Verdana", 26.0)
        undoButton.onAction = Undo
        undoButton.prefWidthProperty().bind(tempScroll.widthProperty())
        undoButton.prefHeightProperty().bind(playerFrame.heightProperty())
        tempScroll.prefWidthProperty().bind(outerGrid.widthProperty().divide(3))
        tempScroll.prefHeightProperty().bind(mainFrame.heightProperty())
        moveFrame.prefWidthProperty().bind(tempScroll.widthProperty())
        moveFrame.prefHeightProperty().bind(tempScroll.heightProperty())
        playerFrame.prefWidthProperty().bind(outerGrid.widthProperty())
        playerFrame.prefHeightProperty().bind(outerGrid.heightProperty().divide(10))
        playerFrame.alignment = Pos.CENTER
        mainFrame.prefHeightProperty().bind(outerGrid.heightProperty())
        mainFrame.prefWidthProperty().bind(outerGrid.widthProperty())
        outerGrid.addColumn(0,mainFrame)
        outerGrid.addRow(1, playerFrame)
        outerGrid.addColumn(1,tempScroll)
        outerGrid.add(undoButton,1,1)
        this.run {
            scene = Scene(outerGrid,730.0,730.0)
            this.isMaximized = true
            show()
        }
    }
}

// I needed buttons to hold the information of their "positions"
class GameButton(): Button() {
    constructor(row: Int, column: Int) : this() {
        this.position = row to column
        coordOfButton[position!!] = this
    }
    private var position: Pair<Int,Int>? = null

    fun getPlaceAt(): Pair<Int, Int>? {
        return position
    }

}

// This just creates a generic pop up that tells the user information like invalid moves or game completion
class CreateConditionWindow(label: Label, button: Button? = null) : Stage() {
    init {
        val mainFrame = VBox(15.0)
        mainFrame.padding = Insets(20.0, 10.0, 20.0, 10.0)
        mainFrame.alignment = Pos.CENTER
        label.prefWidthProperty().bind(mainFrame.widthProperty())
        mainFrame.apply { children.add(label) }
        if (button != null){
            button.prefWidthProperty().bind(mainFrame.widthProperty())
            button.prefHeightProperty().bind(mainFrame.heightProperty())
            mainFrame.apply { children.add(button) }
        }
        this.run {
            scene = Scene(mainFrame,600.0,130.0)
            show()
        }
    }

}

// This lets players pick the color of their game piece and enters name
class PickColor : Stage(), EventHandler<ActionEvent> {
    private var redValue = 128.0
    private var blueValue = 128.0
    private var greenValue = 128.0
    private val nameEntry = TextField()
    init {
        this.title = "Color Picker"
        val mainFrame = VBox(10.0)
        mainFrame.padding = Insets(20.0, 10.0, 20.0, 10.0)
        mainFrame.alignment = Pos.CENTER
        val nameFrame = HBox(10.0)
        nameFrame.alignment = Pos.CENTER
        val nameLabel = Label("Enter name:")
        nameLabel.font = Font.font("Verdana",20.0)
        nameFrame.apply { children.addAll(nameLabel,nameEntry) }
        val bottomOuterFrame = HBox(15.0)
        bottomOuterFrame.alignment = Pos.CENTER
        val colorShow = VBox()
        colorShow.padding = Insets(20.0, 20.0, 20.0, 20.0)
        colorShow.background = Background(BackgroundFill(Color.rgb(redValue.roundToInt(), greenValue.roundToInt(), blueValue.roundToInt(), 1.0), CornerRadii.EMPTY, Insets.EMPTY))
        val sliderPane = VBox(10.0)
        sliderPane.alignment = Pos.CENTER
        val redFrame = HBox(10.0)
        redFrame.alignment = Pos.CENTER
        val rLabel = Label("R")
        rLabel.font = Font.font("Verdana",20.0)
        val redSlider = Slider(0.0,255.0,128.0)
        redSlider.valueProperty().addListener { _, _, newValue ->
            redValue = newValue!!.toDouble()
            colorShow.background = Background(BackgroundFill(Color.rgb(redValue.roundToInt(), greenValue.roundToInt(), blueValue.roundToInt(), 1.0), CornerRadii.EMPTY, Insets.EMPTY))
        }
        redFrame.apply { children.addAll(rLabel,redSlider) }
        val blueFrame = HBox(10.0)
        blueFrame.alignment = Pos.CENTER
        val bLabel = Label("B")
        bLabel.font = Font.font("Verdana",20.0)
        val blueSlider = Slider(0.0,255.0,128.0)
        blueSlider.valueProperty().addListener { _, _, newValue ->
            blueValue = newValue!!.toDouble()
            colorShow.background = Background(BackgroundFill(Color.rgb(redValue.roundToInt(), greenValue.roundToInt(), blueValue.roundToInt(), 1.0), CornerRadii.EMPTY, Insets.EMPTY))
        }
        blueFrame.apply{ children.addAll(bLabel,blueSlider) }
        val greenFrame = HBox(10.0)
        greenFrame.alignment = Pos.CENTER
        val gLabel = Label("G")
        gLabel.font = Font.font("Verdana",20.0)
        val greenSlider = Slider(0.0,255.0,128.0)
        greenSlider.valueProperty().addListener { _, _, newValue ->
            greenValue = newValue!!.toDouble()
            colorShow.background = Background(BackgroundFill(Color.rgb(redValue.roundToInt(), greenValue.roundToInt(), blueValue.roundToInt(), 1.0), CornerRadii.EMPTY, Insets.EMPTY))
        }
        greenFrame.apply { children.addAll(gLabel,greenSlider) }
        sliderPane.apply { children.addAll(redFrame,blueFrame,greenFrame) }
        bottomOuterFrame.apply { children.addAll(sliderPane,colorShow) }
        val okButton = Button("Ok")
        okButton.prefWidthProperty().bind(sliderPane.widthProperty())
        okButton.onAction = this
        mainFrame.apply { children.addAll(nameFrame, bottomOuterFrame, okButton) }
        this.run {
            scene = Scene(mainFrame,350.0,200.0)
            show()
        }
    }

    override fun handle(event: ActionEvent?) {
        val player = Players(nameEntry.text,redValue,blueValue,greenValue)
        player.addLabel()
        playerHolder.add(player)
        (event!!.source as Button).scene.window.hide()
        currentPlayer++
        when (currentPlayer) {
            totalPlayers -> GameCreationStage()
            else -> PickColor()
        }
    }
}

// This class holds information on players such as wins, color choice, and name
class Players(name: String,red: Double, blue: Double, green: Double){
    private var playerName = name
    private var wins = 0
    private var colorChoice = Color.rgb(red.roundToInt(), green.roundToInt(), blue.roundToInt())!!

    private var playerWinLabel = Label("Wins: $wins")


    fun addLabel() {
        val tempLabel = Label(playerName)
        tempLabel.textFill = colorChoice
        tempLabel.font = Font.font("Verdana", 20.0)

        playerWinLabel.font = Font.font("Verdana", 16.0)
        playerWinLabel.text = "Wins: $wins"

        val currentFrame = VBox(5.0)
        currentFrame.alignment = Pos.CENTER_LEFT
        currentFrame.apply { children.addAll(tempLabel, playerWinLabel) }

        playerFrame.apply {
            children.add(currentFrame)
        }
    }

    fun updateWinLabel(){
        wins++
        playerWinLabel.font = Font.font("Verdana",16.0)
        playerWinLabel.text = "Wins: $wins"
    }

    fun getColor(): Color{
        return colorChoice
    }

    fun getCurrentName(): String {
        return playerName
    }

}

// Event handler for placing a game piece in the internal board when a button is clicked
object ButtonPlace: EventHandler<ActionEvent>{
    override fun handle(event: ActionEvent?) {
        val currButton = event!!.source as GameButton
        if (currentGame!!.getState() == 0) {
            try {
                currentGame!!.place(currButton.getPlaceAt()!!.first, currButton.getPlaceAt()!!.second)
                val temp = playerHolder[currentPlayer - 1].getColor().toString().split("x")[1]
                currButton.style = "-fx-background-color: #$temp;"
                moveList.add((currentPlayer to currButton.getPlaceAt()) as Pair<Int, Pair<Int, Int>>)
                moveLabelMaker(currButton.getPlaceAt()!!)
                //currButton.isDisable = true
            } catch (k: ErrorMessage) {
                /*
            val message = k.getError()
            val tempLabel = Label("Error: $message")
            tempLabel.font = Font.font("Verdana",30.0)
            tempLabel.textFill = Color.RED
            tempLabel.alignment = Pos.CENTER
            CreateConditionWindow(tempLabel)
            */

            }
            currentPlayer = currentGame!!.getPlayer()

            if (currentGame!!.getState() != 0) {
                when (currentGame!!.getState()) {
                    totalPlayers + 1 -> {
                        val tempLabel = Label("Game ended in a draw")
                        tempLabel.font = Font.font("Verdana", 30.0)
                        tempLabel.alignment = Pos.CENTER
                        val tempButton = Button("Ok")
                        tempButton.style = "-fx-padding: 20;"
                        tempButton.onAction = NewGame
                        CreateConditionWindow(tempLabel, tempButton)
                    }
                    else -> {
                        val won = playerHolder[currentPlayer - 1]
                        won.updateWinLabel()
                        val tempLabel = Label("${won.getCurrentName()} has won the game")
                        tempLabel.font = Font.font("Verdana", 30.0)
                        tempLabel.alignment = Pos.CENTER
                        val tempButton = Button("Ok")
                        tempButton.style = "-fx-padding: 20;"
                        tempButton.onAction = NewGame
                        CreateConditionWindow(tempLabel, tempButton)
                    }
                }

            }
        }
    }

}

// Event handler for creating a new game
object NewGame: EventHandler<ActionEvent>{
    override fun handle(event: ActionEvent?) {
        val sourceButton = event!!.source as Button
        val source = sourceButton.scene
        val sourceWindow = source.window as Stage
        sourceWindow.hide()
        currentGameGui!!.hide()
        val newGame = InnerTTT(rowNum, colNum, winNum, totalPlayers)
        currentGame = newGame
        newGame.createGame()
        currentPlayer = newGame.getPlayer()
        TTTGui(currentGame!!)
    }
}

// Even handler for removing the most recent move
object Undo: EventHandler<ActionEvent>{
    override fun handle(event: ActionEvent?) {
        val lastMove = moveList.removeAt(moveList.size-1)
        val tempButton = coordOfButton[lastMove.second]
        currentGame!!.undoMove(lastMove.second.first-1,lastMove.second.second-1)
        currentPlayer = currentGame!!.getPlayer()
        moveFrame.children.removeAt(moveFrame.children.size-1)
        tempButton!!.style = "-fx-background-color:"
    }
}

// Function that creates a label whenever a player makes a move
fun moveLabelMaker(move: Pair<Int,Int>){
    val tempFrame = HBox()
    //tempFrame.padding = Insets(3.0)
    tempFrame.alignment = Pos.CENTER

    val namePortion = Label(playerHolder[currentPlayer-1].getCurrentName())
    namePortion.textFill = playerHolder[currentPlayer-1].getColor()
    val positionPortion = Label(" placed at Row ${move.first}, Column ${move.second}")

    //tempFrame.prefWidthProperty().bind(moveFrame.widthProperty())

    tempFrame.apply { children.addAll(namePortion, positionPortion) }

    moveFrame.apply { children.addAll(tempFrame) }

}



fun main(args: Array<String>) {
    launch(NumberPlayers::class.java)
}