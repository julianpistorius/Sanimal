/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model.analysis;

import java.time.temporal.ChronoUnit;
import java.util.Date;

import model.UTMCoord;

public class SanimalAnalysisUtils
{
	public static long daysBetween(Date date1, Date date2)
	{
		if (date1 != null && date2 != null)
			return ChronoUnit.DAYS.between(date1.toInstant(), date2.toInstant());
		else
			return 0;
	}

	public static double distanceBetween(double lat1, double lng1, double lat2, double lng2)
	{
		//		var lat1Rad = lat1.toRadians(), lat2Rad = lat2.toRadians(), delta = (lon2-lon1).toRadians(), R = 6371e3; // gives d in metres
		//	    var d = Math.acos( Math.sin(lat1Rad)*Math.sin(lat2Rad) + Math.cos(lat1Rad)*Math.cos(lat2Rad) * Math.cos(delta) ) * R;
		double lat1Rad = Math.toRadians(lat1);
		double lat2Rad = Math.toRadians(lat2);
		double delta = Math.toRadians(lng2 - lng1);
		double R = 6371.000;
		return Math.acos(Math.sin(lat1Rad) * Math.sin(lat2Rad) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.cos(delta)) * R;
	}

	// SEE http://stackoverflow.com/questions/176137/java-convert-lat-lon-to-utm
	public static UTMCoord Deg2UTM(double Lat, double Lon)
	{
		double Easting;
		double Northing;
		int Zone;
		char Letter;
		Zone = (int) Math.floor(Lon / 6 + 31);
		if (Lat < -72)
			Letter = 'C';
		else if (Lat < -64)
			Letter = 'D';
		else if (Lat < -56)
			Letter = 'E';
		else if (Lat < -48)
			Letter = 'F';
		else if (Lat < -40)
			Letter = 'G';
		else if (Lat < -32)
			Letter = 'H';
		else if (Lat < -24)
			Letter = 'J';
		else if (Lat < -16)
			Letter = 'K';
		else if (Lat < -8)
			Letter = 'L';
		else if (Lat < 0)
			Letter = 'M';
		else if (Lat < 8)
			Letter = 'N';
		else if (Lat < 16)
			Letter = 'P';
		else if (Lat < 24)
			Letter = 'Q';
		else if (Lat < 32)
			Letter = 'R';
		else if (Lat < 40)
			Letter = 'S';
		else if (Lat < 48)
			Letter = 'T';
		else if (Lat < 56)
			Letter = 'U';
		else if (Lat < 64)
			Letter = 'V';
		else if (Lat < 72)
			Letter = 'W';
		else
			Letter = 'X';
		Easting = 0.5 * Math.log((1 + Math.cos(Lat * Math.PI / 180) * Math.sin(Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180)) / (1 - Math.cos(Lat * Math.PI / 180) * Math.sin(Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180))) * 0.9996 * 6399593.62 / Math.pow((1 + Math.pow(
				0.0820944379, 2) * Math.pow(Math.cos(Lat * Math.PI / 180), 2)), 0.5) * (1 + Math.pow(0.0820944379, 2) / 2 * Math.pow((0.5 * Math.log((1 + Math.cos(Lat * Math.PI / 180) * Math.sin(Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180)) / (1 - Math.cos(Lat * Math.PI / 180) * Math.sin(
						Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180)))), 2) * Math.pow(Math.cos(Lat * Math.PI / 180), 2) / 3) + 500000;
		Easting = Math.round(Easting * 100) * 0.01;
		Northing = (Math.atan(Math.tan(Lat * Math.PI / 180) / Math.cos((Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180))) - Lat * Math.PI / 180) * 0.9996 * 6399593.625 / Math.sqrt(1 + 0.006739496742 * Math.pow(Math.cos(Lat * Math.PI / 180), 2)) * (1 + 0.006739496742 / 2 * Math.pow(0.5 * Math
				.log((1 + Math.cos(Lat * Math.PI / 180) * Math.sin((Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180))) / (1 - Math.cos(Lat * Math.PI / 180) * Math.sin((Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180)))), 2) * Math.pow(Math.cos(Lat * Math.PI / 180), 2)) + 0.9996
						* 6399593.625 * (Lat * Math.PI / 180 - 0.005054622556 * (Lat * Math.PI / 180 + Math.sin(2 * Lat * Math.PI / 180) / 2) + 4.258201531e-05 * (3 * (Lat * Math.PI / 180 + Math.sin(2 * Lat * Math.PI / 180) / 2) + Math.sin(2 * Lat * Math.PI / 180) * Math.pow(Math.cos(Lat * Math.PI
								/ 180), 2)) / 4 - 1.674057895e-07 * (5 * (3 * (Lat * Math.PI / 180 + Math.sin(2 * Lat * Math.PI / 180) / 2) + Math.sin(2 * Lat * Math.PI / 180) * Math.pow(Math.cos(Lat * Math.PI / 180), 2)) / 4 + Math.sin(2 * Lat * Math.PI / 180) * Math.pow(Math.cos(Lat * Math.PI
										/ 180), 2) * Math.pow(Math.cos(Lat * Math.PI / 180), 2)) / 3);
		if (Letter < 'M')
			Northing = Northing + 10000000;
		Northing = Math.round(Northing * 100) * 0.01;
		return new UTMCoord(Easting, Northing, Zone, Letter);
	}
}
