/**
 * Script to create a horizontal hexagonal grid in QuPath
 * 
 * Based on the Image J script from @mountain_man in the discussion
 * https://forum.image.sc/t/hexagonal-grid-roi-macro/31465/2
 * 
 *   
 *                                      ___   
 * Hexagons arranged horizontally -->  /   \
 *                                     \___/
 * @author Isaac Vieco-Martí
 */


//Enter the length of the hexagon side in microns(length of the hexagon = circumradius of the hexagon)

l = 10




// Get the main QuPath data structures
def imageData = getCurrentImageData()
def hierarchy = imageData.getHierarchy()
def server = imageData.getServer()

// Get the Calibration and pixel size (here is assumed equal width and height of the pixels)
def cal = server.getPixelCalibration()
double pixelWidth = cal.getPixelWidthMicrons()

if (!cal.hasPixelSizeMicrons()) {
  print 'We need the pixel size information here!'
  return
}



//Set the plane
def plane = ImagePlane.getPlane(0,0)

//Get the dimensions of the Image
imageHeight= server.getHeight()
imageWidth = server.getWidth()



 // Length of hexagon side (pixels)
l = l / pixelWidth  

// Hexagon geometry parameters
xIni = 1.0
yIni = Math.sqrt (3.0) / 2.0
xSiz = xIni
ySiz = yIni
xDel = 3.0
yDel = yIni
xOff = 1.5  // offset for every other row of hexagons



// Scale up to get desired size
xIni *= l
yIni *= l
xSiz *= l
ySiz *= l
xDel *= l
yDel *= l
xOff *= l


// Loop over rows of hexagons

iOff = 0 // which staggered row of hexagons

y = yIni

while (y + ySiz < imageHeight) {
    
  // Loop over hexagons in a row
  x = xIni + (iOff % 2) * xOff
  
  while (x + xSiz < imageWidth) {
      
      // Build hexagonal ROI
      x1 = x + xSiz
      y1 = y
      x2 = x + xSiz / 2.0
      y2 = y - ySiz
      x3 = x - xSiz / 2.0
      y3 = y - ySiz
      x4 = x - xSiz
      y4 = y
      x5 = x - xSiz / 2.0
      y5 = y + ySiz
      x6 = x + xSiz / 2.0
      y6 = y + ySiz
      
      //Create an array for the xCoords
      xCoords = [x1,x2,x3,x4,x5,x6]
      
      //Create an array for the yCoords
      yCoords = [y1,y2,y3,y4,y5,y6]
      
             
      //Create ROI
      def polyROI = ROIs.createPolygonROI(xCoords as double[], yCoords as double[], plane);
      
      def annotation = PathObjects.createAnnotationObject(polyROI)
      
      
      addObject(annotation)
      
      
      x += xDel
  }

  iOff++
  y += yDel
}


