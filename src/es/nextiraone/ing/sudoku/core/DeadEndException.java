package es.nextiraone.ing.sudoku.core;

public class DeadEndException extends Exception {
    
    /** Se lanza cuando se comprueba que un sudoku no tiene solucion */

	static final long serialVersionUID = 1;

	public DeadEndException() {
	}

	public DeadEndException(String message) {
		super(message);
	}

	public DeadEndException(Throwable cause) {
		super(cause);
	}

	public DeadEndException(String message, Throwable cause) {
		super(message, cause);
	}
}
