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



def scanForChildren ( groups ):

    global catalogService



    for grp in groups:

        scanForItems ( grp )



    return



def scanForItems ( group ):

    global catalogService # access to catalog



    print "Processing GROUP: ",group.name



    # try to get subgroups

    subGroups = catalogService.findAllChildCategories(group.id)

    #print "subgroup count ",len(subGroups)

    if len(subGroups) > 0:

        scanForChildren ( subGroups )

    else:

        items = catalogService.findProductsForCategory(group.id)

        updateInventory ( items )



    return



def updateInventory ( items ):

    global repository # access to source item repository



    for itm in items:



	x = itm.name

	x = x.encode('utf-8')

	y = unicode(x,'utf-8')

	print "    Updating ", repr(y),

        invItems = repository.findItemInventory ( Integer.toString(itm.sku) )



        if len(invItems) == 0:

            print "!!!No item inventory found in repository, skipping "

            continue



        # set the sku id on each inventory item

        for invItm in invItems:

	    if invItm is not None:

            	invItm.skuId = itm.skuId



        # save the batch

        catalogService.saveItemInventory ( invItems )



        print "...saved item ",repr(y)



    return





print "- - - - - Updating Inventory - - - - - -"



print " >>>>> Update Inventory Start Time <<<<<<"

print datetime.datetime.now()



print "Connecting to AS400...",

repository = AS400ItemRepository()

print "connected: ",repository.connect()



print "Connection to Catalog...",

hessianFactory = HessianProxyFactory()

hessianFactory.user = "jdoe"

hessianFactory.password = "foo"

catalogService = hessianFactory.create( WebCatalogService, "http://localhost:8080/catalog/services/CatalogService" )

print "connected: ", not catalogService.imageExists( "", "cat" )



# start the scan with the parents

parents = catalogService.findAllChildCategories(1)

scanForChildren ( parents )



print "- - - - - Update Complete - - - - - -"



print " >>>>> Update Inventory End Time <<<<<<"

print datetime.datetime.now()



#item = repository.findItem( "100036" )

#print item.name

#print item.descr



repository.disconnect()



sys.exit(0)

#Lifecycle.beginCall() # this is called outside the script

#dashboardService = Component.getInstance( "dashboardService", 1 )

#jobs = dashboardService.getAllJobs()



#for job in jobs:

#    print job.jobName



#Lifecycle.endCall()

#print sys

#a = 42

#print a

#x = 2 + 2

#print "x:",x

