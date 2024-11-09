import javax.swing.JFrame;

public class App {
    public static void main(String[] args) throws Exception{
        int rowCount = 21;
        int columnCount = 19;
        int tileSize = 32;
        int boardWidth = columnCount * tileSize;
        int boardHeight = rowCount * tileSize;

        JFrame frame = new JFrame("Pac Man"); //name of window and object definition
        //frame.setVisible(true); //frame visibility
        frame.setSize(boardWidth,boardHeight); //frame size
        frame.setLocationRelativeTo(null); //frame center screen
        frame.setResizable(false); //don't allow user to resize frame
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //close when x is clicked

        PacMan pacmanGame = new PacMan();
        frame.add(pacmanGame); //add panel to window
        frame.pack(); //full size of JPanel in window
        pacmanGame.requestFocus(); //
        frame.setVisible(true);
    }
}
