/**
 * This script creates a horizontal hexagon inside and outside of a circle
 * 
 * Based on the Image J script from @mountain_man in the discussion
 * https://forum.image.sc/t/hexagonal-grid-roi-macro/31465/2
 * 
 *                                      ___   
 * Hexagons arranged horizontally -->  /   \
 *                                     \___/
 * 
 * @author Isaac Vieco-Martí
 */

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


selectAnnotations()
addShapeMeasurements("MAX_DIAMETER")


//Set the plane
def plane = ImagePlane.getPlane(0,0)

//Get Path Classes
circle = getPathClass("Circle")
hexagonI = getPathClass("Hexagon Inside")
hexagonO = getPathClass("Hexagon Outside")



selectAnnotations()

target = getSelectedObjects()

////////////////////////////////
//Create hexagon inside circle//
////////////////////////////////

target.forEach {
    
    it.setPathClass(circle)
    it.setColor(0,255,0)
    
    //Get the circle centroid X coord
    xCoord= it.getROI().getCentroidX()
   
    //Get the circle centroid Y coord
    yCoord= it.getROI().getCentroidY()
   
    //Get the radius of the circle
    radius = it.getMeasurements().get('Max diameter µm') / 2
    
    //this radius is our major radius of the hexagon.
    l = radius/pixelWidth
    
    
    x1 = xCoord + l
    y1 = yCoord
    
    x2 = xCoord + (l/2)
    y2 = yCoord - ((Math.sqrt (3.0) / 2.0) * l)
    
    x3 = xCoord - (l/2)
    y3 = yCoord - ((Math.sqrt (3.0) / 2.0) * l)
    
    x4 = xCoord - l
    y4 = yCoord 
    
    
    x5 = xCoord - (l/2)
    y5 = yCoord + ((Math.sqrt (3.0) / 2.0) * l)
    
    x6 = xCoord + (l/2)
    y6 = yCoord + ((Math.sqrt (3.0) / 2.0) * l)
    
    //Create an array for the xCoords
    xCoords = [x1,x2,x3,x4,x5,x6]
      
    //Create an array for the yCoords  
    yCoords = [y1,y2,y3,y4,y5,y6]
    
    //Create ROI
    def polyROI = ROIs.createPolygonROI(xCoords as double[], yCoords as double[], plane);
      
    def annotation = PathObjects.createAnnotationObject(polyROI)
      
      
    addObject(annotation)
    
    
    


    }
    


hexagonsInside = getAnnotationObjects().findAll{it.getPathClass() != getPathClass("Circle")}


hexagonsInside.forEach {
    
    it.setPathClass(hexagonI)
    it.setColor(255,0,0)
    
    }
    


/////////////////////////////////
//Create hexagon outside circle//
/////////////////////////////////



target.forEach {
    
    
    xCoord= it.getROI().getCentroidX()
    xCoord= it.getROI().getCentroidX()
    
    //Get the circle centroid Y coord
    yCoord= it.getROI().getCentroidY()
    
    //this radius is our minor radius of the hexagon.
    radius = it.getMeasurements().get('Max diameter µm') / 2
    
    
    //this is to obtain the major radius of the hexagon.
    l = ((radius/pixelWidth)*2)/Math.sqrt(3)
    
    
    x1 = xCoord + l
    y1 = yCoord
    
    x2 = xCoord + (l/2)
    y2 = yCoord - ((Math.sqrt (3.0) / 2.0) * l)
    
    x3 = xCoord - (l/2)
    y3 = yCoord - ((Math.sqrt (3.0) / 2.0) * l)
    
    x4 = xCoord - l
    y4 = yCoord 
    
    
    x5 = xCoord - (l/2)
    y5 = yCoord + ((Math.sqrt (3.0) / 2.0) * l)
    
    x6 = xCoord + (l/2)
    y6 = yCoord + ((Math.sqrt (3.0) / 2.0) * l)
    
    
    //Create an array for the xCoords
    xCoords = [x1,x2,x3,x4,x5,x6]
    
    //Create an array for the yCoords 
    yCoords = [y1,y2,y3,y4,y5,y6]
    
    
    def polyROI = ROIs.createPolygonROI(xCoords as double[], yCoords as double[], plane);
      
    def annotation = PathObjects.createAnnotationObject(polyROI)
      
     //Create ROI  
    addObject(annotation)
    
    
    


    }
    
    
    

hexagonsOutside = getAnnotationObjects().findAll{it.getPathClass() != getPathClass("Circle") && it.getPathClass() != getPathClass("Hexagon Inside")}


hexagonsOutside.forEach {
    
    it.setPathClass(hexagonO)
    it.setColor(0,0,255)
    
    }
    
      
    
    
