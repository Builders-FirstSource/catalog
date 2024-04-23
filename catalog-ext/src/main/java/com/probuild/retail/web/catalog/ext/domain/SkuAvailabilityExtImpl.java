package com.probuild.retail.web.catalog.ext.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.inventory.domain.SkuAvailabilityImpl;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "BLC_SKU_AVAILABILITY")
public class SkuAvailabilityExtImpl {

    private static final Log LOG = LogFactory.getLog(SkuAvailabilityExtImpl.class);

    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName="blPU")
    static protected EntityManager em;

    /** */
    public void setEntityManager(EntityManager entityManager) {
        this.em = entityManager;
    }

    /** Complete image url on server (file name) */
    /*@Id
    @Column(name = "SKU_AVAILABILITY_ID")
    protected String skuAvailabilityId;*/

    @Id
    @GeneratedValue(generator = "skuAvailabilityId", strategy = GenerationType.TABLE)
    @TableGenerator(name = "skuAvailabilityId", table = "SEQUENCE_GENERATOR", pkColumnName = "ID_NAME", valueColumnName = "ID_VAL", pkColumnValue = "skuAvailabilityIdImpl", allocationSize = 50)
    @Column(name = "SKU_AVAILABILITY_ID")
    protected Long id;

    @Column(name = "AVAILABILITY_DATE")
    protected java.util.Date availabilityDate;

    @Column(name = "AVAILABILITY_STATUS")
    protected String availabilityStatus;

    @Column(name = "LOCATION_ID")
    protected Long locId;

    @Column(name = "QTY_ON_HAND")
    protected Integer qtyOnHand;

    @Column(name = "RESERVE_QTY")
    protected Integer reserveQty;

    @Column(name = "SKU_ID")
    protected Long skuId;

    /** Default constructor */
    public SkuAvailabilityExtImpl() {}

    /*public String getSkuAvailabilityId() {
        return skuAvailabilityId;
    }


    public void setSkuAvailabilityId(String skuAvailabilityId) {
        this.skuAvailabilityId = skuAvailabilityId;
    }
*/
    public java.util.Date getAvailabilityDate() {
        return availabilityDate;
    }

    public void setAvailabilityDate(java.util.Date availabilityDate) {
        this.availabilityDate = availabilityDate;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public Long getLocId() {
        return locId;
    }

    public void setLocId(Long locId) {
        this.locId = locId;
    }

    public Integer getQtyOnHand() {
        return qtyOnHand;
    }

    public void setQtyOnHand(int qtyOnHand) {
        this.qtyOnHand = qtyOnHand;
    }

    public Integer getReserveQty() {
        return reserveQty;
    }

    public void setReserveQty(int reserveQty) {
        this.reserveQty = reserveQty;
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((skuId == null) ? 0 : skuId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SkuAvailabilityExtImpl other = (SkuAvailabilityExtImpl) obj;
        if (skuId == null) {
            if (other.skuId != null)
                return false;
        } else if (!skuId.equals(other.skuId))
            return false;
        return true;
    }



}
