/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package launch.utilities;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import launch.game.GeoCoord;
import launch.game.Defs;
import launch.game.GeoRectangle;

/**
 *
 * @author Corbin
 */
public class WaterMap
{
    private int[][] PixelList = null;
    private int lMapHeight;
    private int lMapWidth;
    private String strFileName;
    private GeoRectangle geoBounds;
    private GeoCoord geoNW;
    private GeoCoord geoSE;
    private float fltPixelsPerLat;
    private float fltPixelsPerLong; 
    private static final int YES = 1;
    
    public WaterMap(String strFileName, GeoCoord geoNW, GeoCoord geoSE)
    {
        this.strFileName = strFileName;
        this.geoNW = geoNW;
        this.geoSE = geoSE;
        this.geoBounds = new GeoRectangle(geoNW.GetLatitude(), geoSE.GetLatitude(), geoNW.GetLongitude(), geoSE.GetLongitude());
    }
    
    public void Initialize(BufferedImage image)
    {
        int lMapHeightPixels = image.getHeight();
        int lMapWidthPixels = image.getWidth();
        fltPixelsPerLong = (float)(lMapWidthPixels)/Math.abs(geoNW.GetLongitude() - geoSE.GetLongitude());
        fltPixelsPerLat = (float)(lMapHeightPixels)/Math.abs(geoNW.GetLatitude() - geoSE.GetLatitude());

        // This returns bytes of data starting from the top left of the bitmap
        // image and goes down.
        // Top to bottom. Left to right.
        final byte[] pixels = ((DataBufferByte)image.getRaster()
                .getDataBuffer()).getData();

        int[][] result = new int[lMapHeightPixels][lMapWidthPixels];

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

            if((row == lMapHeightPixels - 1) && (col == lMapWidthPixels - 1))
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

                if(col == lMapWidthPixels)
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
        
        PixelList = result;

        LaunchLog.ConsoleMessage(String.format("%s loaded.", strFileName));
    }
    
    public boolean Initialized()
    {
        return PixelList != null;
    }
    
    public String GetFileName()
    {
        return strFileName;
    }
    
    public boolean ContainsCoordinate(GeoCoord geoPosition)
    {
        return geoBounds.Contains(geoPosition);
    }
    
    public boolean CoordinateIsWater(GeoCoord geoPosition)
    {
        if(!ContainsCoordinate(geoPosition))
        {
            throw new RuntimeException(String.format("WaterMap does not contain coordinate!"));
        }
        else
        {
            int lLatY = LatToImageY(geoPosition.GetLatitude());
            int lLongX = LongToImageX(geoPosition.GetLongitude());
            
            return PixelList[lLatY][lLongX] == YES;
        }
    }
    
    private int LongToImageX(float fltLongitude)
    {
        int x = (int)(Math.abs(fltLongitude - geoNW.GetLongitude()) * fltPixelsPerLong);
        
        //LaunchLog.ConsoleMessage(String.format("Long: %f X: %d", fltLongitude, x));
        return x == 0 ? x : x - 1;
    }
    
    private int LatToImageY(float fltLatitude)
    {
        // get y value
        /*float mercN = (float)(Math.log(Math.tan((Math.PI/4) + (Math.toRadians(fltLatitude)/2))));
        
        float y = (float)((WATER_MAP_HEIGHT/2) - (WATER_MAP_HEIGHT * mercN / (2 * Math.PI)));
        
        int newValue = (int)Math.abs(y);
        //LaunchLog.ConsoleMessage(String.format("Lat: %f Y: %d", fltLatitude, newValue));
        return newValue == 0 ? newValue : newValue - 1;*/
        
        int y = (int)(Math.abs(fltLatitude - geoNW.GetLatitude()) * fltPixelsPerLat);
        
        //LaunchLog.ConsoleMessage(String.format("Lat: %f X: %d", fltLatitude, y));
        return y == 0 ? y : y - 1;
    }
}
