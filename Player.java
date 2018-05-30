package circuitlab;

class Player {
    
    boolean turn;
    int id;
    int currency;
    double voltage;
    
    
    public Player() {
        turn = false;
        id = -1;
        currency = 1000;
        voltage = 0;
        
    }
    
    public Player(boolean t, int i, int c, double v) {
        turn = t;
        id = i;
        currency = c;
        voltage = v;
    }
    
}
