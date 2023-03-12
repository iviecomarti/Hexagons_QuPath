/**
 * Script to create a vertical hexagonal grid overlaying different annotations in QuPath
 * 
 * This script takes the dimensions for each annotation and creates a hexagonal grid of that dimensions. 
 * Then, the grid is placed on the annotation.
 * We can decide if we want the hexagons "intersecting" the annotation or the hexagons "within" the annotation.
 * Default mode "intersects".  
 * 
 * 
 * Based on the Image J script from @mountain_man in the discussion
 * https://forum.image.sc/t/hexagonal-grid-roi-macro/31465/2
 * 
 * Part of script using Geometries from @ImageScientist 
 * https://www.imagescientist.com/editing-object-shapes-or-types
 * 
 *                                      / \   
 * Hexagons arranged vertically -->    |   |
 *                                      \ /
 * 
 * @author Isaac Vieco-Martí
 */

//Enter the length of the hexagon side in microns(length of the hexagon = circumradius of the hexagon)

l = 5




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


 // Length of hexagon side (pixels)
l = l / pixelWidth   

// Hexagon geometry parameters
xIni = Math.sqrt(3.0) / 2.0;
yIni = 1.0;
xSiz = xIni;
ySiz = yIni;
xDel = Math.sqrt(3.0);
yDel = 1.5;
xOff = Math.sqrt(3.0) / 2.0;  // offset for every other row of hexagons



// Scale up to get desired size
xIni *= l
yIni *= l
xSiz *= l
ySiz *= l
xDel *= l
yDel *= l
xOff *= l


//Get Path Classes
target = getPathClass("Target")
hexagon = getPathClass("Hexagon")



selectAnnotations()

targets = getSelectedObjects()

targets.forEach {
   
    //Set the class and cahnge the color, for visual purposes
    it.setPathClass(target)
    it.setColor(0,255,0)
    
    
    //Get the dimensions of the boundig box of the annotation
    //Here the "+(4*l)" is to expand the box one hexagon per part.
    //In the intersect overlay helps to obtain a complete filling
    //However, you can remove "+(4*l)" and it works. It will change a bit the pattern, but it is a minnor adjustment.
    
    widthTarget = it.getROI().getBoundsWidth() + (4*l)
    heightTarget = it.getROI().getBoundsHeight()+ (4*l)
    
    //Get the X and Y coords of the boundig box of the annotation
    //Here the "-(2*l)" is to move the starting point one hexagon.
    //In the intersect overlay helps to obtain a complete filling.   
    //However you can remove "-(2*l)" and it works. It will change a bit the pattern, but it is a minnor adjustment.
    //Hexagons will be moved to those coords
    
    int originXTarget = it.getROI().getBoundsX() - (2*l) as int
    int originYTarget = it.getROI().getBoundsY() - (2*l) as int
      
    //If you remove the "+(4*l)", also remove "-(2*l)" 
    
    
    
    
    // Loop over rows of hexagons

    iOff = 0 // which staggered row of hexagons

    y = yIni

    while (y + ySiz < heightTarget) {
    
      // loop over hexagons in a row
      x = xIni + (iOff % 2) * xOff
  
          while (x + xSiz < widthTarget) {
              // build hexagonal ROI
              x1 = x ;
              y1 = y-ySiz;
              x2 = x - xSiz;
              y2 = y - ySiz / 2.0;
              x3 = x - xSiz;
              y3 = y + ySiz / 2.0;
              x4 = x;
              y4 = y + ySiz;
              x5 = x + xSiz;
              y5 = y + ySiz / 2.0 ;
              x6 = x + xSiz;
              y6 = y - ySiz / 2.0;
              
              
              //Create an array for the xCoords
              xCoords = [x1,x2,x3,x4,x5,x6]
              
              //Create an array for the yCoords
              yCoords = [y1,y2,y3,y4,y5,y6]
             
              //Create the ROI
              def polyROI = ROIs.createPolygonROI(xCoords as double[], yCoords as double[], plane);
              
              //Move the ROI to the annotation coords
              polyROI = polyROI.translate(originXTarget,originYTarget)
      
              def annotation = PathObjects.createAnnotationObject(polyROI)
      
      
              addObject(annotation)
      
      
              x += xDel
          }

      iOff++
      y += yDel
    }    
    
   
    
 }



//Look for the hexagonal annotations and change the class.

hexagons = getAnnotationObjects().findAll{it.getPathClass() != getPathClass("Target")}


hexagons.forEach {
    
    it.setPathClass(hexagon)
    //Change the color for visual purposes
    it.setColor(255,0,0)
    
    }



//This part of the code is adapted from 
//https://www.imagescientist.com/editing-object-shapes-or-types

//Merge all targets in one object to obtain the geometry of all the targets
selectObjectsByClassification("Target")
mergeSelectedAnnotations()

targetAnnotation = getAnnotationObjects().find{it.getPathClass() == getPathClass("Target")}
targetGeom = targetAnnotation.getROI().getGeometry()


//Cycle through ALL OTHER annotations to check for intersection
//We do want a list to put all of these into so we are not removing them one at a time (slow!)

toRemove = []



getAnnotationObjects().findAll{it.getPathClass() != getPathClass("Target")}.each{anno->
    currentGeom = anno.getROI().getGeometry()
    
    //Note the ! which means we are looking for NOT intersects
    //You can choose between "intersects" or "within" to remove hexagons.
    if ( !currentGeom.intersects(targetGeom) ) {
        
        //What we need to remove is the annotation, NOT the geometry, which is only the shape of the object
        
        toRemove << anno
    }
}

//remove the objects that were collected
removeObjects(toRemove,true)
fireHierarchyUpdate()



//Finally, split the targets again
selectObjectsByClassification("Target")
runPlugin('qupath.lib.plugins.objects.SplitAnnotationsPlugin', '{}')






