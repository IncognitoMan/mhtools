/*  MHP2GDEC v1.0 - MH TMH image extractor
    Copyright (C) 2011 Codestation

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package dec;


import img.Gim;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;


import base.Decoder;
import base.EndianFixer;

/**
 * ExtractPluginD v1.0
 * 
 * @author Codestation
 */
public class ExtractPluginD extends EndianFixer implements Decoder {
    
    @Override
    public void extract(String filename) {
        String directory = filename.split("\\.")[0];
        new File(directory).mkdir();
        try {
            FileInputStream file = new FileInputStream(filename);
            extract(file, directory);
            file.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void extract(FileInputStream file, String directory) {
        try {            
            byte header_id[] = new byte[8];
            file.read(header_id);
            int header_gim_count = readInt(file);
            file.skip(4);
            //file.readInt(); //32 bit padding
            for(int i = 0; i < header_gim_count; i++) {
                Gim gim = new Gim();
                gim.load(file);
                String fileformat;
                String format;
                String fileout;
                if(gim.getDataType() == Gim.GIM_TYPE_PALETTE)
                    format = "palette";
                else if(gim.getDataType() == Gim.GIM_TYPE_PIXELS)
                    format = "pixels";
                else if(gim.getDataType() == Gim.GIM_TYPE_NOPALETTE)
                    format = "none";
                else
                    format = "image";
                String palette = null;
                if(gim.getDataType() != Gim.GIM_TYPE_NOPALETTE) {
                    if(gim.getPaletteType() == Gim.RGBA8888) {
                        palette = "RGBA8888";
                    } else {
                        palette = "RGBA5551";  
                    }
                }
                if(gim.isSupported()) {
                    int buffered_type = BufferedImage.TYPE_INT_ARGB;
                    BufferedImage bi = new BufferedImage(gim.getWidth(), gim.getHeight(), buffered_type);
                    bi.setRGB(0, 0, gim.getWidth(), gim.getHeight(), gim.getRGBarray(), 0, gim.getWidth());
                    fileformat = "png";
                    fileout = String.format(directory + "/%03d", i) + "_" + format + "_" + palette + "." + fileformat;
                    System.out.println("Extracting " + fileout);
                    File out = new File(fileout);
                    out.delete();
                    ImageIO.write(bi,fileformat, out);
                } else {
                    fileformat = "gim";
                    if(palette != null)
                        fileout = String.format(directory + "/%03d", i) + "_" + format + "_" + palette + "." + fileformat;
                    else
                        fileout = String.format(directory + "/%03d", i) + "_" + format + "." + fileformat;
                    System.out.println("Extracting " + fileout);
                    FileOutputStream out = new FileOutputStream(fileout);
                    gim.write(out);
                    out.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
