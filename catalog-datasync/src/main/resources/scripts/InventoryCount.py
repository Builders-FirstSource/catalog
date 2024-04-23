from com.probuild.retail.web.catalog.repository import AS400ItemRepository
from com.probuild.retail.web.catalog.domain import Item
from com.probuild.retail.web.catalog.domain import ItemInventory
from org.jboss.seam import Component
from org.jboss.seam.contexts import Lifecycle
from com.caucho.hessian.client import HessianProxyFactory
from com.probuild.retail.web.catalog.ext.service import WebCatalogService
from java.lang import Integer
import sys
import datetime

groupItemCount = 0

def topScanForChildren ( groups ):
    global catalogService
    global groupItemCount
    
    for grp in groups:
        groupItemCount = 0
        print "Category " + grp.name    
        scanForItems ( grp )
        print "Category " + grp.name + " Count of items ",  groupItemCount
    return

def scanForChildren ( groups ):
    global catalogService
    
    for grp in groups:
        scanForItems ( grp )

    return

def scanForItems ( group ):
    global catalogService # access to catalog
    global groupItemCount
    
    # try to get subgroups
    subGroups = catalogService.findAllChildCategories(group.id)
    #print "subgroup count ",len(subGroups)
    if len(subGroups) > 0:
        scanForChildren ( subGroups )
    else:
        items = catalogService.findProductsForCategory(group.id)
        #print "Items: " , items

        print "Items in GROUP: " + group.name.encode('utf8')  + " " , len(items)
        groupItemCount += len(items)        
        
    return



print "- - - - - Counting Inventory - - - - - -"

print " >>>>> Count Inventory Start Time <<<<<<"
print datetime.datetime.now()

print "Connecting to AS400...",
repository = AS400ItemRepository()
print "connected: ",repository.connect()

print "Connection to Catalog..."
hessianFactory = HessianProxyFactory()
hessianFactory.user = "jdoe"
hessianFactory.password = "foo"
catalogService = hessianFactory.create( WebCatalogService, "http://localhost:8080/catalog/services/CatalogService" )

# start the scan with the parents
parents = catalogService.findAllChildCategories(1)
topScanForChildren ( parents )

print " >>>>> Count Inventory End Time <<<<<<"
print datetime.datetime.now()


repository.disconnect()

sys.exit(0)
