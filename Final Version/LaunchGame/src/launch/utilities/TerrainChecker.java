/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package launch.utilities;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import launch.game.GeoCoord;

/**
 *
 * @author Corbin
 */
public final class TerrainChecker
{
    private static List<WaterMap> HighResMaps; //Watermaps for specific important areas that we want to be really igh resolution. For example, pearl harbor.
    private static int[][] LowResWaterMap;     //A comparatively low resolution map for the whole world.
    private static float WATER_PIXELS_PER_COORDINATE_LONG;
    private static int WATER_MAP_HEIGHT;
    private static int WATER_MAP_WIDTH;
    private static final int YES = 1; //1 if the coordinate is whatever type of terrain is being checked.
    
    private static float MOST_NORTHERLY_LAND_LATITUDE = 83.646889f;
    private static float MOST_SOUTHERLY_WATER_LATITUDE = -78.72f;
    
    public static boolean CoordinateIsWater(GeoCoord geoPosition)
    {
        for(WaterMap map : HighResMaps)
        {
            if(map.ContainsCoordinate(geoPosition))
            {
                return map.CoordinateIsWater(geoPosition);
            }
        }
        
        if(geoPosition.GetLatitude() > MOST_NORTHERLY_LAND_LATITUDE)
            return true;
        else if(geoPosition.GetLatitude() < MOST_SOUTHERLY_WATER_LATITUDE)
            return false;
        
        try
        {
            return LowResWaterMap[WaterLatToImageY(geoPosition.GetLatitude())][(WaterLongToImageX(geoPosition.GetLongitude()))] == YES;
        }
        catch(Exception ex)
        {
            try
            {
                float fltLongToCheck = geoPosition.GetLongitude();
                
                if(fltLongToCheck > 180) 
                {
                    fltLongToCheck -= 360;
                }
                else if(fltLongToCheck < -180)
                {
                    fltLongToCheck += 360;
                }
                
                return LowResWaterMap[WaterLatToImageY(geoPosition.GetLatitude())][(WaterLongToImageX(fltLongToCheck))] == YES;
            }
            catch(Exception ex2)
            {
                LaunchLog.ConsoleMessage(String.format("Coordinate Lat %f, Long %f Invalid.", geoPosition.GetLatitude(), geoPosition.GetLongitude()));

                return false;
            }
        }
    }
    
    private static int WaterLongToImageX(float fltLongitude)
    {
        int x = (int)((fltLongitude + 180) * WATER_PIXELS_PER_COORDINATE_LONG);
        
        //LaunchLog.ConsoleMessage(String.format("Long: %f X: %d", fltLongitude, x));
        return x == 0 ? x : x - 1;
    }
    
    private static int WaterLatToImageY(float fltLatitude)
    {
        // get y value
        float mercN = (float)(Math.log(Math.tan((Math.PI/4) + (Math.toRadians(fltLatitude)/2))));
        
        float y = (float)((WATER_MAP_HEIGHT/2) - (WATER_MAP_HEIGHT * mercN / (2 * Math.PI)));
        
        int newValue = (int)Math.abs(y);
        //LaunchLog.ConsoleMessage(String.format("Lat: %f Y: %d", fltLatitude, newValue));
        return newValue == 0 ? newValue : newValue - 1;
    }
    
    //Call this directly in XMLGameLoader.
    public static void LoadHighResMaps(List<WaterMap> newHighResMaps)
    {
        HighResMaps = newHighResMaps;
    }
    
    public static void InitializeMaps()
    {
        LaunchLog.ConsoleMessage("Initializing maps...");

        try
        {
            for(WaterMap map : HighResMaps)
            {
                FileInputStream imgInput = new FileInputStream("maps//" + map.GetFileName());
                BufferedImage image = ImageIO.read(imgInput);

                if(!map.Initialized())
                {
                    map.Initialize(image);
                }
                else
                {
                    LaunchLog.ConsoleMessage(String.format("Could not load %s.", map.GetFileName()));
                } 
            }

            FileInputStream imgInput = new FileInputStream("maps//water_map.bmp");
            BufferedImage imgWater = ImageIO.read(imgInput);

            WATER_MAP_HEIGHT = imgWater.getHeight();
            WATER_MAP_WIDTH = imgWater.getWidth();
            WATER_PIXELS_PER_COORDINATE_LONG = (float)(WATER_MAP_WIDTH)/360f;

            //LaunchLog.ConsoleMessage(String.format("Water map pixels per longitude: %f", WATER_PIXELS_PER_COORDINATE_LONG));

            // This returns bytes of data starting from the top left of the bitmap
            // image and goes down.
            // Top to bottom. Left to right.
            final byte[] pixels = ((DataBufferByte)imgWater.getRaster()
                    .getDataBuffer()).getData();

            final int width = imgWater.getWidth();
            final int height = imgWater.getHeight();

            int[][] result = new int[height][width];

            boolean done = false;
            boolean alreadyWentToNextByte = false;
            int byteIndex = 0;
            int row = 0;
            int col = 0;
            int numBits = 0;
            byte currentByte = pixels[byteIndex];

            while(!done)
            {
                alreadyWentToNextByte = false;

                result[row][col] = (currentByte & 0x80) >> 7;
                currentByte = (byte) (((int) currentByte) << 1);
                numBits++;

                if((row == height - 1) && (col == width - 1))
                {
                    done = true;
                }
                else
                {
                    col++;

                    if(numBits == 8)
                    {
                        currentByte = pixels[++byteIndex];
                        numBits = 0;
                        alreadyWentToNextByte = true;
                    }

                    if(col == width)
                    {
                        row++;
                        col = 0;

                        if(!alreadyWentToNextByte)
                        {
                            currentByte = pixels[++byteIndex];
                            numBits = 0;
                        }
                    }
                }
            }

            LowResWaterMap = result;
            LaunchLog.ConsoleMessage("Water map loaded.");
        }
        catch(IOException ex)
        {
            LaunchLog.ConsoleMessage("Could not load water map.");
        }
    }
}
