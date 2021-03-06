package command.field;

import java.awt.*;
import command.field.units.Unit;

public class Board {
    private final static int BoardWidth = Window.getActualWindowWidth()-Window.getActualWindowWidth()/4;
    private final static int BoardHeight = Window.getActualWindowHeight()-Window.getActualWindowHeight()/22;
    private final static int NUM_ROWS = 36;
    private final static int NUM_COLUMNS = 48;
    private static Tile board[][] = new Tile[NUM_ROWS][NUM_COLUMNS];
    public final static int ydelta = Window.getHeight2()/NUM_ROWS;
    public final static int xdelta = BoardWidth/NUM_COLUMNS;
    public static int seed = (int)(Math.random()*(0x7fffffff));

    public static void Reset() {
        for (int zi = 0; zi<NUM_ROWS; zi++) {
            for (int zx = 0; zx<NUM_COLUMNS; zx++) {
                board[zi][zx] = null;
            }
        }
    }    

    public static void Draw(Graphics2D g) {
        //Draw tiles
        for (int row=0; row<NUM_ROWS; row++) {
            for (int col=0; col<NUM_COLUMNS; col++) {
                if(board[row][col] != null) {
                    board[row][col].draw(g);
                }
            }
        }
        
        //Draw grid
        if(CommandField.inGame) {
            g.setColor(new Color(105,105,105));
            for (int zi = 0;zi<NUM_ROWS;zi++) {
                g.drawLine(Window.getX(0), Window.getY(zi*ydelta)+(Window.getActualWindowHeight()-BoardHeight), Window.getX(BoardWidth), Window.getY(zi*ydelta)+(Window.getActualWindowHeight()-BoardHeight));
            }

            for (int zi = 1;zi<NUM_COLUMNS+1 && zi*xdelta<=BoardWidth;zi++) {
                g.drawLine(Window.getX(zi*xdelta), Window.getY((Window.getActualWindowHeight()-BoardHeight)), Window.getX(zi*xdelta), Window.getY(Window.getHeight2()));
            }

            for (int i=1; i<NUM_COLUMNS; i++) {
                g.setColor(Color.black);
                g.setFont(new Font("Arial", Font.PLAIN, 5));
                g.drawString(""+i, Window.getX(i*xdelta)+xdelta/2, Window.getY((Window.getActualWindowHeight()-BoardHeight))+ydelta/2);
            }

            for (int i=1; i<NUM_ROWS; i++) {
                g.setColor(Color.black);
                g.setFont(new Font("Arial", Font.PLAIN, 5));
                g.drawString(""+i, (Window.getX(0)+xdelta/2), Window.getY(i*ydelta)+ydelta/2+(Window.getActualWindowHeight()-BoardHeight));
            }
            
            Board.ColourBorder(g);
        }
    }
    
    public static void BoardInit() {
        Reset();
        seed = (int)(Math.random()*(0x7fffffff));
        for (int row=1; row<NUM_ROWS; row++) {
            for (int col=1; col<NUM_COLUMNS && col*xdelta<=BoardWidth; col++) {
                board[row][col] = new Tile(Window.getX(col*xdelta), Window.getY(row*ydelta)+(Window.getActualWindowHeight()-BoardHeight), PerlinNoise.GetHeight(Window.getX(col*xdelta), Window.getY(row*ydelta), seed));
            }
        }
    }
    
    public static void CheckCollision(int _xPos, int _yPos) {
        if (CommandField.inGame && _xPos > Window.getX(xdelta) && _yPos > Window.getY((Window.getActualWindowHeight()-BoardHeight)+ydelta) && _xPos < Window.getX(xdelta*NUM_COLUMNS) && _yPos < Window.getY(ydelta*(NUM_ROWS+2))) {
            int col = (_xPos-Window.getX(0))/xdelta;
            int row = ((_yPos-Window.getY(0))/ydelta)-2;
            if(board[row][col] != null) {
                if(Menu.GetMenuType() != Menu.MenuType.UNIT_SELECTION && Menu.GetMenuType() != Menu.MenuType.UNIT_MOVEMENT && Menu.GetMenuType() != Menu.MenuType.UNIT_ATTACK && !CommandField.gameOver) {
                    Menu.SetMenuType(Menu.MenuType.UNIT_INFO);
                }
                
                Button.ClearButtons();
                Menu.SetSelection(board[row][col]);
            }
        }
    }
    
    public static int GetBoardHeight() {
        return BoardHeight;
    }
    
    public static int GetBoardWidth() {
        return BoardWidth;
    }
    
    public static Tile GetTileOf(int row, int col) {
        return(board[row+1][col]);
    }
    
    public static void ShadeTilesTurn(Player _playerTurn) {
        for (int row=(NUM_ROWS/2)*_playerTurn.getPlayerNumberRaw(); row<NUM_ROWS-((NUM_ROWS/2)*(1-_playerTurn.getPlayerNumberRaw())); row++) {
            for (int col=0; col<NUM_COLUMNS; col++) {
                if(board[row][col] != null) {
                    board[row][col].setShaded(true, new Color(0,0,0,155));
                }
            }
        }

        for (int row=(NUM_ROWS/2)*Player.GetNextTurnRaw(); row<NUM_ROWS-((NUM_ROWS/2)*(1-Player.GetNextTurnRaw())); row++) {
            for (int col=0; col<NUM_COLUMNS; col++) {
                if(board[row][col] != null) {
                    board[row][col].setShaded(false);
                }
            }
        }
        
        for(int col=0; col<NUM_COLUMNS; col++) {
            if(board[NUM_ROWS/2][col] != null) {
                board[NUM_ROWS/2][col].setShaded(true, new Color(0,0,0,155));
            }
        }
    }
    
    public static boolean AllowedPlacementTiles(Player _playerTurn, int _row, int _column) {
        return(board[_row+1][_column] != null && !board[_row+1][_column].isShaded());
    }
    
    public static void ClearShadedTiles() {
        for (int row=0; row<NUM_ROWS; row++) {
            for (int col=0; col<NUM_COLUMNS; col++) {
                if(board[row][col] != null) {
                    board[row][col].setShaded(false);
                }
            }
        }
    }
    
    public static void ColourBorder(Graphics2D g) {
        if(Menu.GetSelectedTile() != null) {
            g.setColor(Color.red);
            g.drawRect(Menu.GetSelectedTile().getXPos(), Menu.GetSelectedTile().getYPos(), xdelta, ydelta);
        }
    }
    
    public static int GetNumColumns() {
        return NUM_COLUMNS;
    }
    
    public static int GetNumRows() {
        return NUM_ROWS;
    }
    
    public static void CheckUnitBoardEnd() {
        for(int col=1; col<Board.GetNumColumns(); col++) {
            for(Unit unit : Player.GetPlayer(0).getUnits()) {
                if(board[1][col].getUnit() != null && board[1][col].getUnit() == unit && !Player.GetPlayer(0).getWinUnits().contains(unit)) {
                    Player.GetPlayer(0).getWinUnits().add(unit);
                }
            }
            
            for(Unit unit : Player.GetPlayer(1).getUnits()){
                if(board[35][col].getUnit() != null && board[35][col].getUnit() == unit && !Player.GetPlayer(1).getWinUnits().contains(unit)) {
                    Player.GetPlayer(1).getWinUnits().add(unit);
                }
            }
        }
    }
}
