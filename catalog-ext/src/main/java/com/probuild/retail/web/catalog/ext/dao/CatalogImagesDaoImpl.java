package com.probuild.retail.web.catalog.ext.dao;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;




/**
 * The Class Images is the data access class for the JPG Image database table.
 * <br>
 * This class uses a Hibernate implementation of JPA configured through
 * annotations. The Entity references the following tables: PB_IMAGES
 * @author
 */
@Entity
@Table(name = "PB_IMAGES")
public class CatalogImagesDaoImpl {

    private static final Log LOG = LogFactory.getLog(CatalogImagesDaoImpl.class);

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName="blPU")
    static protected EntityManager em;

    /** */
    public void setEntityManager(EntityManager entityManager) {
    	this.em = entityManager;
   	}
    /** Product ID for image */

    @Column(name = "PRODUCT_ID")
    protected Long productId;

    /** Indicates if the image is the large or small jpg */
    @Column(name = "NAME")
    protected String name;

    /** Indicates if image is category or product image - 'C' indicated category image, 'P' - product image */
    @Column(name = "TYPE")
    protected char type;

    /** Complete image url on server (file name) */
    @Id
    @Column(name = "URL")
    protected String imageURL;

    /** Small JPG image */
    @Column(name = "IMAGE")
    protected byte[] image;

    /** Date/Time of import of images */
    @Column(name = "IMAGE_DATE")

    //protected Date imageDate;
    protected java.sql.Timestamp imageDate;

    //Indicates if it is new Image (can take values as Y / N)
    @Column(name = "NEW")
    protected char newImage;


    /** Default constructor */
    public CatalogImagesDaoImpl() {}

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public char getType() { return type; }
    public void setType(char type) { this.type = type; }

    public String getURL() { return imageURL; }
    public void setURL(String imageURL) { this.imageURL = imageURL; }

    public byte[] getImage() { return image; }
    public void setImage(byte[] image) { this.image = image; }

    public Timestamp getImageDate() { return imageDate; }
    public void setImageDate(Timestamp imageDate) { this.imageDate = imageDate; }

    public char getNewImage() { return newImage; }
    public void setNewImage(char newImage) { this.newImage = newImage; }


    /**
     * Convert file into image
     */
    public static byte[] convertFileToImage(String fileName) {

    	File file = new File("C:\\mavan-hibernate-image-mysql.gif");
    	byte[] bFile = new byte[(int) file.length()];

    	try {
    		FileInputStream fileInputStream = new FileInputStream(file);

    		//	convert file into array of bytes
    		fileInputStream.read(bFile);
    		fileInputStream.close();
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}

    	return bFile;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass())
            return false;

        CatalogImagesDaoImpl other = (CatalogImagesDaoImpl) obj;

        if (type != other.type) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result;
        return result;
    }

	private static void save(CatalogImagesDaoImpl ci) {
		// TODO Auto-generated method stub

	}
}
