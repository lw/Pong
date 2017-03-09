/*  
 *  Copyright (C) 2010  Luca Wehrstedt
 *
 *  This file is released under the GPLv2
 *  Read the file 'COPYING' for more information
 */

import javax.swing.JPanel;
import javax.swing.JOptionPane;
import java.awt.Color;
import java.awt.Graphics;

import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;


/**
 * Class containing all game logic.
 */
public class Pong extends JPanel implements ActionListener, MouseListener, KeyListener {

	/**
	 * Constant for the radius of the ball.
	 */
	private static final int RADIUS = 10;

	/**
	 * Initial speed of the ball in pixels per frame.
	 */
	private static final int START_SPEED = 9;

	/**
	 * Number of frames between each acceleration of the ball. ACCELERATION = 125 corresponds to an acceleration every
     * 2.5 seconds.
	 */
	private static final int ACCELERATION = 125;



    /**
     * Speed at which the paddle moves.
     */
	private static final int SPEED = 12;

    /**
     * Height of the paddle.
     */
    private static final int HEIGHT = 50;

    /**
     * Width of the paddle.
     */
    private static final int WIDTH = 20;

    /**
     *
     */
    private static final int TOLERANCE = 5;

    /**
     * The "padding" surrounding the edges of the screen that the ball can bounce off of.
     */
    private static final int PADDING = 10;


    /**
     * Player object used to describe the type of player controlling the first paddle.
     */
    private Player player1;

    /**
     * Player object used to describe the type of player controlling the second paddle.
     */
    private Player player2;


    /**
     * Used to determine whether the draw method should create a fresh instance of the game.
     */
    private boolean new_game = true;

    /**
     * X-Coordinate of the ball.
     */
    private int ball_x;

    /**
     * Y-Coordinate of the ball.
     */
    private int ball_y;

    /**
     * Velocity of the ball in the x direction.
     */
    private double ball_x_speed;

    /**
     * Veolicty of the ball in the y direction.
     */
	private double ball_y_speed;

    /**
     * Enable or disable acceleration
     */
	public boolean acceleration = false;

    /**
     * The number of frames since the last acceleration.
     */
    private int ball_acceleration_count;


    /**
     * Tracks whether the mouse is in or outside the window.
     */
    private boolean mouse_inside = false;

    /**
     * If the up key is pressed, key up is true.
     */
    private boolean key_up = false;

    /**
     * If the down key is pressed, key down is true.
     */
    private boolean key_down = false;


    /**
     * Called upon the start of a new game to create a new window and initialize the players.
     * @param p1_type Integer representing the type of player 1. 0 = EASY_CPU, 1 = HARD_CPU, 2 = human using mouse,
     *                3 = human using keyboard
     * @param p2_type Integer representing the type of player 2. Uses same representations as p1_type.
     */
    public Pong (int p1_type, int p2_type) {
		super ();
		setBackground (new Color (0, 0, 0));
		
		player1 = new Player (p1_type);
		player2 = new Player (p2_type);
	}

    /**
     * Method used in order to determine the direction of travel for the HARD AI. The destination of the paddle is
     * determined by extrapolating to determine where the ball will be upon collision.
     * @param player
     */
    private void computeDestination (Player player) {
		int base; //Unused
		if (ball_x_speed > 0)
			player.destination = ball_y + (getWidth() - PADDING - WIDTH - RADIUS - ball_x) * (int)(ball_y_speed) / (int)(ball_x_speed);
		else
			player.destination = ball_y - (ball_x - PADDING - WIDTH - RADIUS) * (int)(ball_y_speed) / (int)(ball_x_speed);
		
		if (player.destination <= RADIUS)
			player.destination = 2 * PADDING - player.destination;
		
		if (player.destination > getHeight() - 10) {
			player.destination -= RADIUS;
			if ((player.destination / (getHeight() - 2 * RADIUS)) % 2 == 0)
				player.destination = player.destination % (getHeight () - 2 * RADIUS);
			else
				player.destination = getHeight() - 2 * RADIUS - player.destination % (getHeight () - 2 * RADIUS);
			player.destination += RADIUS;
		}
	}

    /**
     * Changes the position of the paddle. Moves by a maximum distance controlled by the SPEED constant.
     * @param player Player who's paddle moves.
     * @param destination Destination to move the paddle to. If the destination is further than the speed of the paddle
     *                    allows, the paddle will travel the furthest distance possible in one frame.
     */private void movePlayer (Player player, int destination) {
		int distance = Math.abs (player.position - destination);
		
		if (distance != 0) {
			int direction = - (player.position - destination) / distance;
			
			if (distance > SPEED)
				distance = SPEED;
			
			player.position += direction * distance;
			
			if (player.position - HEIGHT < 0)
				player.position = HEIGHT;
			if (player.position + HEIGHT > getHeight())
				player.position = getHeight() - HEIGHT;
		}
	}

    /**
     * Computes the destination of the paddle for the next frame depending on which type of player is moving the
     * current paddle.
     * @param player
     */private void computePosition (Player player) {
		//If the player type is mouse, move the paddle towards the mouse cursor.
		if (player.getType() == Player.MOUSE) {
			if (mouse_inside) {
				int cursor = getMousePosition().y;
				movePlayer (player, cursor);
			}
		}
		// If the player type is keyboard, move the paddle in the direction pressed.
		else if (player.getType() == Player.MOUSE) {
			if (key_up && !key_down) {
				movePlayer (player, player.position - SPEED);
			}
			else if (key_down && !key_up) {
				movePlayer (player, player.position + SPEED);
			}
		}
		// If paddle is controlled by a hard CPU, move the paddle to the optimal precomputed player destination.
		else if (player.getType() == Player.CPU_HARD) {
			movePlayer (player, player.destination);
		}
		// If the paddle is controlled by an easy CPU, move the paddle towards the ball.
		else if (player.getType() == Player.CPU_EASY) {
			movePlayer (player, ball_y);
		}
	}

    /**
     * Fired once every 20 ms. Calculates the new position of the ball and paddles and redraws them at that location.
     * @param g Graphics object used to render the ball and paddles.
     */
    // Draw
	public void paintComponent (Graphics g) {
		super.paintComponent (g);
		
		// If this is the first frame to be rendered.
		if (new_game) {
            //Place the ball in the center of the screen
			ball_x = getWidth () / 2;
			ball_y = getHeight () / 2;


			double phase = Math.random () * Math.PI / 2 - Math.PI / 4;
			ball_x_speed = (int)(Math.cos (phase) * START_SPEED);
			ball_y_speed = (int)(Math.sin (phase) * START_SPEED);
			
			ball_acceleration_count = 0;
			
			if (player1.getType() == Player.CPU_HARD || player1.getType() == Player.CPU_EASY) {
				player1.position = getHeight () / 2;
				computeDestination (player1);
			}
			if (player2.getType() == Player.CPU_HARD || player2.getType() == Player.CPU_EASY) {
				player2.position = getHeight () / 2;
				computeDestination (player2);
			}
			
			new_game = false;
		}
		
		//Calculate the position of the first paddle.
		if (player1.getType() == Player.MOUSE || player1.getType() == Player.KEYBOARD || ball_x_speed < 0)
			computePosition (player1);
		
		//Calculate the postion of the second paddle.
		if (player2.getType() == Player.MOUSE || player2.getType() == Player.KEYBOARD || ball_x_speed > 0)
			computePosition (player2);
		
		//Calculate the position of the ball based on it's current location and it's current speed.
		ball_x += ball_x_speed;
		ball_y += ball_y_speed;
		if (ball_y_speed < 0) // Hack to fix double-to-int conversion
			ball_y ++;
		
		//The ball will accelerate after a fixed number of frames.
		if (acceleration) {
			ball_acceleration_count ++;
			if (ball_acceleration_count == ACCELERATION) {
				ball_x_speed = ball_x_speed + (int)ball_x_speed / Math.hypot ((int)ball_x_speed, (int)ball_y_speed) * 2;
				ball_y_speed = ball_y_speed + (int)ball_y_speed / Math.hypot ((int)ball_x_speed, (int)ball_y_speed) * 2;
				ball_acceleration_count = 0;
			}
		}
		
		/* Collision detection on the left side of the screen. Determines if the players paddle was there to deflect,
		 * if not, player 2 scores a point */
		if (ball_x <= PADDING + WIDTH + RADIUS) { //If the ball is in contact with the left side of the screen
            //Determine the coordinates of the balls point of collision.
			int collision_point = ball_y + (int)(ball_y_speed / ball_x_speed * (PADDING + WIDTH + RADIUS - ball_x));
			if (collision_point > player1.position - HEIGHT - TOLERANCE &&  //If the paddle was in the way
                    // Redirect the ball
			    collision_point < player1.position + HEIGHT + TOLERANCE) {
				ball_x = 2 * (PADDING + WIDTH + RADIUS) - ball_x;
				ball_x_speed = Math.abs (ball_x_speed);
				ball_y_speed -= Math.sin ((double)(player1.position - ball_y) / HEIGHT * Math.PI / 4)
				                * Math.hypot (ball_x_speed, ball_y_speed);
				if (player2.getType() == Player.CPU_HARD)
					computeDestination (player2);
			}
			else { //Otherwise award a point to player 2
				player2.points ++;
				new_game = true;
			}
		}
		
		/* Collision detection on the right side of the screen. Determines if the players paddle was there to deflect,
		 * if not, player one scores a point */
		if (ball_x >= getWidth() - PADDING - WIDTH - RADIUS) {
			int collision_point = ball_y - (int)(ball_y_speed / ball_x_speed * (ball_x - getWidth() + PADDING + WIDTH + RADIUS));
			if (collision_point > player2.position - HEIGHT - TOLERANCE && 
			    collision_point < player2.position + HEIGHT + TOLERANCE) {
				ball_x = 2 * (getWidth() - PADDING - WIDTH - RADIUS ) - ball_x;
				ball_x_speed = -1 * Math.abs (ball_x_speed);
				ball_y_speed -= Math.sin ((double)(player2.position - ball_y) / HEIGHT * Math.PI / 4)
				                * Math.hypot (ball_x_speed, ball_y_speed);
				if (player1.getType() == Player.CPU_HARD)
					computeDestination (player1);
			}
			else {
				player1.points ++;
				new_game = true;
			}
		}
		
		//Collision detection for the top of the screen
		if (ball_y <= RADIUS) {
			ball_y_speed = Math.abs (ball_y_speed);
			ball_y = 2 * RADIUS - ball_y;
		}
		
		//Collision detection for the bottom of the screen
		if (ball_y >= getHeight() - RADIUS) {
			ball_y_speed = -1 * Math.abs (ball_y_speed);
			ball_y = 2 * (getHeight() - RADIUS) - ball_y;
		}
		
		//Drawing the paddles
		g.setColor (Color.WHITE);
		g.fillRect (PADDING, player1.position - HEIGHT, WIDTH, HEIGHT * 2);
		g.fillRect (getWidth() - PADDING - WIDTH, player2.position - HEIGHT, WIDTH, HEIGHT * 2);
		
		// Drawing the ball
		g.fillOval (ball_x - RADIUS, ball_y - RADIUS, RADIUS*2, RADIUS*2);
		
		// Drawing the score
		g.drawString (player1.points+" ", getWidth() / 2 - 20, 20);
		g.drawString (player2.points+" ", getWidth() / 2 + 20, 20);
	}

    /**
     * Method called every 20ms by the timer in PongWindow.java. Generates a new frame each time it is called
     * @param e
     */
	public void actionPerformed (ActionEvent e) {
		repaint ();
	}

    /**
     * Toggles mouse_inside property to true when the mouse enters the screen.
     * @param e
     */
	public void mouseEntered (MouseEvent e) {
		mouse_inside = true;
	}

    /**
     * Toggles mouse_inside property to false when the mouse exits the screen.
     * @param e
     */
	public void mouseExited (MouseEvent e) {
		mouse_inside = false;
	}

    /**
     * Method required by mouse listener interface. Unused.
     * @param e
     */
	public void mousePressed (MouseEvent e) {}

    /**
     * Method required by mouse listener interface. Unused.
     * @param e
     */
    // Mouse released
	public void mouseReleased (MouseEvent e) {}

    /**
     * Method required by mouse listener interface. Unused.
     * @param e
     */
	public void mouseClicked (MouseEvent e) {}

    /**
     * Method called whenever a key is pressed.
     * Toggles the key_up and key_down booleans used to track whether the paddles should be moving.
     * @param e
     */
	public void keyPressed (KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_UP)
			key_up = true;
		else if (e.getKeyCode() == KeyEvent.VK_DOWN)
			key_down = true;
	}

    /**
     * Method called whenever a key is released.
     * Toggles the key_up and key_down booleans used to track whether the paddles should be moving.
     * @param e KeyEvent passed by the JFrame. Used to determine the keystroke that occurred.
     */
	public void keyReleased (KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP)
			key_up = false;
		else if (e.getKeyCode() == KeyEvent.VK_DOWN)
			key_down = false;
	}

    /**
     * Method required by keyboard listener interface. Unused.
     * @param e
     */
	public void keyTyped (KeyEvent e) {}
}
