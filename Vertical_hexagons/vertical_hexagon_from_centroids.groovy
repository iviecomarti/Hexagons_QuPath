/**
 * This script creates a vertical hexagon surrounding an annotation
 * by using the centroid of the annotation as reference
 * 
 * Based on the Image J script from @mountain_man in the discussion
 * https://forum.image.sc/t/hexagonal-grid-roi-macro/31465/2
 * 
 *                                      / \   
 * Hexagons arranged vertically -->    |   |
 *                                      \ /
 * 
 * @author Isaac Vieco-Martí
 */

//Enter the length of the hexagon side in microns(length of the hexagon = circumradius of the hexagon)
l = 20


// Get the main QuPath data structures
def imageData = getCurrentImageData()
def hierarchy = imageData.getHierarchy()
def server = imageData.getServer()

// Get the Calibration and pixel size
def cal = server.getPixelCalibration()
double pixelWidth = cal.getPixelWidthMicrons()

if (!cal.hasPixelSizeMicrons()) {
  print 'We need the pixel size information here!'
  return
}



//Set the plane
def plane = ImagePlane.getPlane(0,0)

 // Length of hexagon side (pixels)
l = l / pixelWidth  


//Get Path Classes
target = getPathClass("Target")
hexagon = getPathClass("Hexagon")


selectAnnotations()

targets = getSelectedObjects()

targets.forEach {
    
    it.setPathClass(target)
    it.setColor(0,255,0)
    
    
    //Get the centroid coord X
    xCoord= it.getROI().getCentroidX()
    
    //Get the centroid coord Y
    yCoord= it.getROI().getCentroidY()
        
    
    x1 = xCoord 
    y1 = yCoord - l
    
    x2 = xCoord - ((Math.sqrt (3.0) / 2.0) * l)
    y2 = yCoord - l/2
    
    x3 = xCoord- ((Math.sqrt (3.0) / 2.0) * l)
    y3 = yCoord + l/2
    
    x4 = xCoord 
    y4 = yCoord + l
    
    
    x5 = xCoord + ((Math.sqrt (3.0) / 2.0) * l)
    y5 = yCoord + l/2
    
    x6 = xCoord + ((Math.sqrt (3.0) / 2.0) * l)
    y6 = yCoord - l/2
    
    //Create an array for the xCoords
    xCoords = [x1,x2,x3,x4,x5,x6]
    
    //Create an array for the yCoords  
    yCoords = [y1,y2,y3,y4,y5,y6]
    
    
    //Create ROI
    def polyROI = ROIs.createPolygonROI(xCoords as double[], yCoords as double[], plane);
      
    def annotation = PathObjects.createAnnotationObject(polyROI)
      
      
    addObject(annotation)
    
    
    


    }




//Look for the hexagonal annotations and change the class.

hexagons = getAnnotationObjects().findAll{it.getPathClass() != getPathClass("Target")}


hexagons.forEach {
    
    it.setPathClass(hexagon)
    //Change the color for visual purposes
    it.setColor(255,0,0)
    
    }
