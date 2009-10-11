package org.ncombat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import org.ncombat.components.Ship;

	/**
	 * @author lmiller
	 *
	 */
	public class NCombatMain {

		static Ship s;
		static StringTokenizer st;
		static String cmd = new String();
		static int units;
		static int units2;
		/**
		 * @param args
		 */
		public static void main(String[] args) {
			NCombatMain r = new NCombatMain();
			// System.out.println(r.ncombatMethod());
			s = new Ship();
			
			try {
			   	System.out.println(s.getStatus());
				System.out.println("cmd: \b");
	            BufferedReader is = new BufferedReader(new InputStreamReader(System.in));
	            String inputLine;
	            while ((inputLine = is.readLine( )) != null) {            	
	            	if (inputLine.equalsIgnoreCase("stop")){
	                	System.exit(1);
	                }
	            	else {
	            	processCommands(inputLine);
	            	System.out.println(s.getStatus());
	        
	            	}	
	            }
	            is.close( );
	        } catch (IOException e) {
	            System.out.println("IOException: " + e);
	        }
			
		}

		/**
		 * @param inputLine
		 */
		private static void processCommands(String cmdLine) {
			// this should be a regex parser
			// Ad,d
			// Ld+
			// Md
			// Rd
			
			if (cmdLine.equals(null)) {
				System.out.println("No command.");
			}
			else {
				st = new StringTokenizer(cmdLine);
				
				while (st.hasMoreTokens()) {
					cmd = st.nextToken();
					System.out.println("cmd: " + cmd + "\tprefix: " + cmd.substring(0, 1));
					
					try {
					
						// A - Accelerate ship. Ax,y  
						if (cmd.substring(0,1).equalsIgnoreCase("A")) {
							units = Integer.parseInt(cmd.substring(1,2));
							units2 = Integer.parseInt(cmd.substring(3));
							s.accelerate(units, units2);
							System.out.println("accelerate: " + cmd);
							
						}
						
						// M - fire missile. MX,Y or MX MY
						else if (cmd.substring(0,1).equalsIgnoreCase("M")) {
							units = Integer.parseInt(cmd.substring(1));
							s.fireMissile(units);
							System.out.println("fireMissile: " + cmd);
							
						}
						
						
						else if (cmd.substring(0,1).equalsIgnoreCase("L")) {
							units = Integer.parseInt(cmd.substring(1));
							s.fireLaser(units);
							System.out.println("fireLaser: " + cmd);
							
						}
						
						// R - rotate ship. RX,y
						else if (cmd.substring(0,1).equalsIgnoreCase("R")) {
							units = Integer.parseInt(cmd.substring(1));
							s.rotate(units);
							System.out.println("rotating: " + units);
							
						}
					}
					catch (NumberFormatException e) {
						System.out.println("Unrecognized command : " + cmd);
					}
					catch (StringIndexOutOfBoundsException e1) {
						System.out.println("Unrecognized command : " + cmd);
					}
				}
			}
		}
	}
