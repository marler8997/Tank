package jmar.games;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

public class GLTexture {	
	public final int textureID;
	public final int width,height;
	
	public final int imageRowByteLength;
	public final int imageTotalByteLength;
	
	public GLTexture(InputStream imageInputStream, int glTextureUnit) throws IOException, GLException {
		ByteBuffer imageRGBABuffer;
		
		// Link the PNG decoder to this stream
		PNGDecoder decoder = new PNGDecoder(imageInputStream);
		
		// Get the width and height of the texture
		this.width = decoder.getWidth();
		this.height = decoder.getHeight();
		this.imageRowByteLength = 4 * this.width;
		this.imageTotalByteLength = this.imageRowByteLength * this.height;
		
		// Decode the PNG file in a ByteBuffer
		imageRGBABuffer = ByteBuffer.allocateDirect(imageTotalByteLength);
		decoder.decode(imageRGBABuffer, this.imageRowByteLength, Format.RGBA);
		imageRGBABuffer.flip();
		
		// Create a new texture object in memory and bind it
		this.textureID = GL11.glGenTextures();
		
		GL13.glActiveTexture(glTextureUnit);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textureID);
		
		// All RGB bytes are aligned to each other and each component is 1 byte
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
		
		// Upload the texture data and generate mip maps (for scaling)
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, this.width, this.height, 0, 
				GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, imageRGBABuffer);
		GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
		
		// Setup the ST coordinate system
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		
		// Setup what to do when the texture has to be scaled
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);		
		
		GLException.throwOnError();
	}
}
