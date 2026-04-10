/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package launch.game;

/**
 *
 * @author conta
 */
public class QuadtreeCell 
{
    public double dblNorthernLat;
    public double dblSouthernLat;
    public double dblWesternLong;
    public double dblEasternLong;
    public double dblCenterLat;
    public double dblCenterLong;
    //public double fltYDim;       //How wide on the Y axis is this rectangle?
    //public double fltXDim;       //How tall on the X axis is this rectangle?
    
    //The first cell, a global one.
    public QuadtreeCell(double dblNorthernLat, double dblSouthernLat, double dblWesternLong, double dblEasternLong)
    {
        this.dblNorthernLat = dblNorthernLat;
        this.dblSouthernLat = dblSouthernLat;
        this.dblWesternLong = dblWesternLong;
        this.dblEasternLong = dblEasternLong;
        this.dblCenterLat = ((dblNorthernLat - dblSouthernLat)/2) + dblSouthernLat;
        this.dblCenterLong = ((dblEasternLong - dblWesternLong)/2) + dblWesternLong;
    }
    
    //A child cell.
    /*public QuadtreeCell(GeoCoord geoNW, GeoCoord geoNE, GeoCoord geoSW, GeoCoord geoSE, double fltYDim, double fltXDim)
    {
        this.geoNW = geoNW;
        this.geoNE = geoNE;
        this.geoSW = geoSW;
        this.geoSE = geoSE;
        this.geoCenter = geoNE.GetCopy();
        geoCenter.MoveToward(geoSW, (geoCenter.DistanceTo(geoSW)/2));
        this.fltYDim = geoSE.DistanceTo(geoNE);
        this.fltXDim = geoNE.DistanceTo(geoNW);
    }*/
    
    public boolean ContainsCoordinate(GeoCoord geoCoord)
    {
        return geoCoord.IsInsideGeoRect(GetSW(), GetNE());
    }
    
    public QuadtreeCell GetNWQuad()
    {
        return new QuadtreeCell(dblNorthernLat, dblCenterLat, dblWesternLong, dblCenterLong);
    }
    
    public QuadtreeCell GetNEQuad()
    {
        return new QuadtreeCell(dblNorthernLat, dblCenterLat, dblCenterLong, dblEasternLong);
    }
    
    public QuadtreeCell GetSWQuad()
    {
        return new QuadtreeCell(dblCenterLat, dblSouthernLat, dblWesternLong, dblCenterLong);
    }
    
    public QuadtreeCell GetSEQuad()
    {
        return new QuadtreeCell(dblCenterLat, dblSouthernLat, dblCenterLong, dblEasternLong);
    }
    
    public boolean CollidesWithCircle(GeoCoord circleCenter, float fltRadius)
    {
        GeoCoord circleEdge = circleCenter.GetCopy();
        circleEdge.Move(circleCenter.BearingTo(GetCenter()), fltRadius);
        
        /*
         * If this rectangle contains the coordinate, then the circle hits the square. 
         * If the bearing between the circle edge and rectangle center is different 
         * than the bearing between the circle center and the rectangle center, than 
         * the square lies entirely inside the circle.
         */
        return ContainsCoordinate(circleEdge) || (int)circleEdge.BearingTo(GetCenter()) != (int)circleCenter.BearingTo(GetCenter());
    }
    
    public GeoCoord GetSW()
    {
        return new GeoCoord(dblSouthernLat, dblWesternLong, true);
    }
    
    public GeoCoord GetNE()
    {
        return new GeoCoord(dblNorthernLat, dblEasternLong, true);
    }
    
    public GeoCoord GetCenter()
    {
        return new GeoCoord(dblCenterLat, dblCenterLong, true);
    }
}
