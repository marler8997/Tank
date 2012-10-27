package jmar.games;

import java.io.File;
import java.io.IOException;
import java.nio.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import javax.imageio.*;

import jmar.JmarMath;

/**
 * Loads an image from file, stores pixels as ARGB int array, and RGBA ByteBuffer for
 * use in OpenGL.  Can convert images to power-of-2 dimensions for textures.
 * <P>
 * Static functions are included to load, flip and convert pixel arrays.
 * <P>
 */

public class GLImageMaybe {
    public int height = 0;
    public int width = 0;
    public ByteBuffer pixelBuffer = null;  // store pixels as bytes in GL_RGBA format
    public int[] pixels = null;            // store pixels as ARGB integers

    public int widthAsPowerOf2;
    public int heightAsPowerOf2;
    
    public int textureHandle;
    
    public GLImageMaybe(BufferedImage image) throws InterruptedException {
    	this(image, true, false);
    }
    
    public GLImageMaybe(BufferedImage image, boolean flipYaxis, boolean convertPow2) throws InterruptedException {    	
        if (flipYaxis) image = flipY(image);
        if (convertPow2) image = convertToPowerOf2(image);
        
        width = image.getWidth(null);
        height = image.getHeight(null);
        
        pixels = getImagePixels(image);  // pixels in default Java ARGB format
        pixelBuffer = convertImagePixelsRGBA(pixels,width,height,false);  // convert to bytes in RGBA format
        widthAsPowerOf2 = JmarMath.getNearestPowerOfTwo(width); // the texture size big enough to hold this image
        heightAsPowerOf2 = JmarMath.getNearestPowerOfTwo(height); // the texture size big enough to hold this image
    }
    
    /**
     * Create GLImage from pixels passed in a ByteBuffer.  This is a non-standard approach
     * that may give unpredictable results.
     * @param pixels
     * @param w
     * @param h
     */
    public GLImageMaybe(ByteBuffer gl_pixels, int w, int h) {
		if (gl_pixels != null) {
       		this.pixelBuffer = gl_pixels;
        	this.pixels = null;
        	this.height = h;
        	this.width = w;
		}
    }

    /**
     * return true if image has been loaded successfully
     * @return
     */
    public boolean isLoaded()
    {
        return (pixelBuffer != null);
    }
    
    public void flipPixelsOnYAxis()
    {
        pixels = flipPixels(pixels, width, height);
    }

    /**
     * Return the Image pixels in default Java int ARGB format.
     * @return
     * @throws InterruptedException 
     */
    public static int[] getImagePixels(Image image) throws InterruptedException
    {
    	int[] pixelsARGB = null;
        if (image != null) {
        	int imgw = image.getWidth(null);
        	int imgh = image.getHeight(null);
        	pixelsARGB = new int[ imgw * imgh];
            PixelGrabber pg = new PixelGrabber(image, 0, 0, imgw, imgh, pixelsARGB, 0, imgw);
            pg.grabPixels();
        }
        return pixelsARGB;
    }

    /**
     * return int array containing pixels in ARGB format (default Java byte order).
     */
    public int[] getPixelInts()
    {
        return pixels;
    }

    /**
     * return ByteBuffer containing pixels in RGBA format (commmonly used in OpenGL).
     */
    public ByteBuffer getPixelBytes()
    {
        return pixelBuffer;
    }

    //========================================================================
    //
    // Static convertion functions to prepare pixels for use in OpenGL
    //
    //========================================================================

    /**
     * Flip an array of pixels vertically
     * @param imgPixels
     * @param imgw
     * @param imgh
     * @return int[]
     */
    public static int[] flipPixels(int[] imgPixels, int imgw, int imgh)
    {
        int[] flippedPixels = null;
        if (imgPixels != null) {
            flippedPixels = new int[imgw * imgh];
            for (int y = 0; y < imgh; y++) {
                for (int x = 0; x < imgw; x++) {
                    flippedPixels[ ( (imgh - y - 1) * imgw) + x] = imgPixels[ (y * imgw) + x];
                }
            }
        }
        return flippedPixels;
    }

    /**
     * Copy ARGB pixels to a ByteBuffer without changing the ARGB byte order. If used to make a
     * texture, the pixel format is GL12.GL_BGRA.  With this format we can leave pixels in ARGB
     * order (faster), but unfortunately I had problems building mipmaps in BGRA format
     * (GLU.gluBuild2DMipmaps() did not recognize GL_UNSIGNED_INT_8_8_8_8 and
     * GL_UNSIGNED_INT_8_8_8_8_REV types so screwed up the BGRA/ARGB byte order on Mac).
     *
     * @return ByteBuffer
     */
    public static ByteBuffer convertImagePixelsARGB(int[] jpixels, int width, int height, boolean flipOnYAxis) {
    	// flip Y axis
        if (flipOnYAxis) {
            jpixels = flipPixels(jpixels, width, height); // flip Y axis
        }
        
        // put int pixels into Byte Buffer
    	ByteBuffer byteBuffer = ByteBuffer.allocateDirect(jpixels.length * 4).order(ByteOrder.nativeOrder());
    	byteBuffer.asIntBuffer().put(jpixels);
        return byteBuffer;
    }

    /**
     * Convert ARGB pixels to a ByteBuffer containing RGBA pixels.  The GL_RGBA format is
     * a default format used in OpenGL 1.0, but requires that we move the Alpha byte for
     * each pixel in the image (slow).  Would be better to use OpenGL 1.2 GL_BGRA format
     * and leave pixels in the ARGB format (faster) but this pixel format caused problems
     * when creating mipmaps (see note above).
     * .<P>
     * If flipVertically is true, pixels will be flipped vertically (for OpenGL coord system).
     * @return ByteBuffer
     */
    public static ByteBuffer convertImagePixelsRGBA(int[] jpixels, int imgw, int imgh, boolean flipVertically) {
        byte[] bytes;     // will hold pixels as RGBA bytes
        if (flipVertically) {
            jpixels = flipPixels(jpixels, imgw, imgh); // flip Y axis
        }
        bytes = convertARGBtoRGBA(jpixels);
        return allocBytes(bytes);  // convert to ByteBuffer and return
    }

    /**
     * Convert pixels from java default ARGB int format to byte array in RGBA format.
     * @param jpixels
     * @return
     */
    public static byte[] convertARGBtoRGBA(int[] jpixels)
    {
        byte[] bytes = new byte[jpixels.length*4];  // will hold pixels as RGBA bytes
        int p, r, g, b, a;
        int j=0;
        for (int i = 0; i < jpixels.length; i++) {
            p = jpixels[i];
            a = (p >> 24) & 0xFF;  // get pixel bytes in ARGB order
            r = (p >> 16) & 0xFF;
            g = (p >> 8) & 0xFF;
            b = (p >> 0) & 0xFF;
            bytes[j+0] = (byte)r;  // fill in bytes in RGBA order
            bytes[j+1] = (byte)g;
            bytes[j+2] = (byte)b;
            bytes[j+3] = (byte)a;
            j += 4;
        }
        return bytes;
    }

    //========================================================================
    // Utility functions
    //========================================================================

    /**
     * Same function as in GLApp.java.  Allocates a ByteBuffer to hold the given
     * array of bytes.
     *
     * @param bytearray
     * @return  ByteBuffer containing the contents of the byte array
     */
    public static ByteBuffer allocBytes(byte[] bytearray) {
        ByteBuffer bb = ByteBuffer.allocateDirect(bytearray.length).order(ByteOrder.nativeOrder());
        bb.put(bytearray).flip();
        return bb;
    }

    /**
     * Scale this GLImage so width and height are powers of 2.  Recreate pixels and pixelBuffer.
     * @throws InterruptedException 
     */
    public void convertToPowerOf2() throws InterruptedException {
    	// make BufferedImage from original pixels
    	BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    	image.setRGB(0, 0, width, height, pixels, 0, width);

    	// scale into new image
    	BufferedImage scaledImg = convertToPowerOf2(image);

    	// resample pixel data
    	width = scaledImg.getWidth(null);
        height = scaledImg.getHeight(null);
        pixels = getImagePixels(scaledImg);  // pixels in default Java ARGB format
        pixelBuffer = convertImagePixelsRGBA(pixels,width,height,false);  // convert to bytes in RGBA format
        widthAsPowerOf2 = JmarMath.getNearestPowerOfTwo(width); // the texture size big enough to hold this image
        heightAsPowerOf2 = JmarMath.getNearestPowerOfTwo(height); // the texture size big enough to hold this image
    }

	/**
	 * Save an array of ARGB pixels to a PNG file.
	 * If flipY is true, flip the pixels on the Y axis before saving.
	 * @throws IOException 
	 */
	public static void savePixelsToPNG(int[] pixels, int width, int height, String imageFilename, boolean flipY) throws IOException {
		if (flipY) {
			// flip the pixels vertically (opengl has 0,0 at lower left, java is upper left)
			pixels = GLImageMaybe.flipPixels(pixels, width, height);
		}
		
		// Create a BufferedImage with the RGB pixels then save as PNG
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		image.setRGB(0, 0, width, height, pixels, 0, width);
		ImageIO.write(image, "png", new File(imageFilename));
	}

    //========================================================================
    // Static functions to flip and scale images
    //========================================================================

    /**
     * Scale the given BufferedImage to width and height that are powers of two.
     * Return the new scaled BufferedImage.
     */
    public static BufferedImage convertToPowerOf2(BufferedImage bsrc) {
        // find powers of 2 equal to or greater than current dimensions
        int newW = JmarMath.getNearestPowerOfTwo(bsrc.getWidth());
        int newH = JmarMath.getNearestPowerOfTwo(bsrc.getHeight());
        if (newW == bsrc.getWidth() && newH == bsrc.getHeight()) {
        	return bsrc;    // no change necessary
        }
        else {
	        AffineTransform at = AffineTransform.getScaleInstance((double)newW/bsrc.getWidth(),(double)newH/bsrc.getHeight());
	        BufferedImage bdest = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
	        Graphics2D g = bdest.createGraphics();
	        g.drawRenderedImage(bsrc,at);
	        return bdest;
        }
    }

    /**
     * Scale the given BufferedImage to the given width and height.
     * Return the new scaled BufferedImage.
     */
    public static BufferedImage scale(BufferedImage bsrc, int width, int height) {
        AffineTransform at = AffineTransform.getScaleInstance((double)width/bsrc.getWidth(),(double)height/bsrc.getHeight());
        BufferedImage bdest = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bdest.createGraphics();
        g.drawRenderedImage(bsrc,at);
        return bdest;
    }

    /**
     * Flip the given BufferedImage vertically.
     * Return the new flipped BufferedImage.
     */
    public static BufferedImage flipY(BufferedImage bsrc) {
        AffineTransform affineTransform = AffineTransform.getScaleInstance(1, -1);
        affineTransform.translate(0, -bsrc.getHeight(null));
        AffineTransformOp op = new AffineTransformOp(affineTransform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return op.filter(bsrc, null);
    }

}
