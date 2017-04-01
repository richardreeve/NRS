/*
 * Copyright (C) 2004 Ben Torben-Nielsen
 *
 *    This program is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU General Public License as
 *    published by the Free Software Foundation; either version 2 of
 *    the License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public
 *    License along with this program; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA 02111-1307 USA
 *
 * For further information in the first instance contact:
 * Richard Reeve <richardr@inf.ed.ac.uk>
 *
 */
package nrs.audio.simulator ;

import nrs.audio.simulator.* ;
import java.io.File;
import javax.swing.filechooser.FileFilter;

public class DSEarFileFilter extends FileFilter 
{

	public static final int EAR = 1 ;
	public static final int PNG = 2 ;
	public static final int SVG = 3 ;
	public static final int TXT = 4 ;
	
	private int type ;

    public DSEarFileFilter(int type) 
    {
    	this.type = type ;
    }

    public boolean accept(File f) 
    {
        if (f != null) 
        {
            if (f.isDirectory()) 
            {
                return true;
	        }
            String extension = getExtension(f);
            
            switch(type)
            {
            	case EAR :
            	{
            		if (extension != null && extension.equalsIgnoreCase("txt")) 
            			return true ;	
            	} ; break ;	
            	case PNG :
            	{
            		if (extension != null && extension.equalsIgnoreCase("png")) 
            			return true ;	
            	} ; break ;	
            	case SVG :
            	{
            		if (extension != null && extension.equalsIgnoreCase("svg")) 
            			return true ;	
            	} ; break ;	
            	case TXT :
            	{
            		if (extension != null && extension.equalsIgnoreCase("txt")) 
            			return true ;	
            	} ; break ;	
            	default :
            	{
            		return false ;	
            	}          	
            }
        }
        
        return false ;
    }

    private String getExtension(File f) 
    {
        if(f != null) 
        {
            String filename = f.getName();
            int i = filename.lastIndexOf('.');
            if (i > 0 && i < filename.length()-1) 
            {
                return filename.substring(i+1).toLowerCase();
            }
        }
        return null;
    }

    public String getDescription() 
    {
	    if(type == EAR)
	    	return "Ear configuration file (*.txt)" ;
	    if(type == PNG)
	    	return "Portable Network Graphics (*.png)" ;
	    if(type == SVG)
	    	return "Scalable Vector Graphics (*.svg)" ;
	    if(type == TXT)
	    	return "Scalable Vector Graphics (*.svg)" ;
	    	
	    return "" ;
    }
}
