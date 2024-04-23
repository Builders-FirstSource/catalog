package com.probuild.retail.web.catalog.upload;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probuild.retail.web.catalog.desktop.support.Configuration;
import com.probuild.retail.web.catalog.desktop.util.FileUtil;
import com.probuild.retail.web.catalog.domain.ImportedItem;
import com.probuild.retail.web.catalog.domain.Item;
import com.probuild.retail.web.catalog.domain.ItemFilter;
import com.probuild.retail.web.catalog.domain.ItemGroup;
import com.probuild.retail.web.catalog.domain.ItemImage;
import com.probuild.retail.web.catalog.domain.ItemInventory;
import com.probuild.retail.web.catalog.ext.service.WebCatalogService;
import com.probuild.retail.web.catalog.repository.AS400ItemRepository;

public class Importer {
    private static final Logger logger =
                            LoggerFactory.getLogger( Importer.class );

    public static final String ITEM_EXISTS = "Item already exists in catalog";
    public static String SMALL_IMAGE = "";
    public static String LARGE_IMAGE = "";
    public static String LARGE_SMALL = "";

    private String IMG_DIR = "C:/java_dev/catalog/images/";
    private static final String SERVER_IMG_PATH_SMALL = "/images/products/small/";
    private static final String SERVER_IMG_PATH_LARGE = "/images/products/large/";
    private static final String SERVER_IMG_PATH_CATEGORY = "/images/categories/small/";

    private static ClassValidator<ImportedItem> itemValidator =
                        new ClassValidator<ImportedItem>( ImportedItem.class );

    private List<ImportProblem> problems;
    private List<ImportInfo> messages;

    private String currentStatus;

    private Map<String, ItemGroup> groupCache;

    private WebCatalogService service;
    public AS400ItemRepository AS400repository;

    public Importer( WebCatalogService service, Configuration config ) {
        super();

        this.service = service;

        AS400repository = new AS400ItemRepository();
        if(AS400repository.connect()) {
            System.out.println("AS400Repository Connection success");
        } else {
            System.out.println("AS400Repository Connection failed");
        }

        problems = new ArrayList<ImportProblem>();
        messages = new ArrayList<ImportInfo>();

        groupCache = new HashMap<String, ItemGroup>();

        IMG_DIR = config.getImageFolder();
        logger.info( "Using base dir: {}", IMG_DIR );


    }


    @SuppressWarnings("unchecked")
    public int addToCatalog ( List<? extends Item> items ) {

        problems.clear();
        messages.clear();

        // going to send items list to catalog server for addition
        for ( Item item : items ) {

            if ( !isGroupSaved( item.getGroup() ) ) {
                // save the group
                logger.debug( "saving group {}", item.getGroup() );
                saveGroup ( item.getGroup() );
                logger.debug( "group {} Id is now {}", item.getGroup(), item.getGroup().getId() );
            }

            // check if group has items attached
            if ( service.findAllChildCategories(
                            item.getGroup().getId() ).size() > 0 ) {
                problems.add( new ImportProblem.Builder().
                        item( item ).
                        message( "Assigned item group can not be used " +
                                "because it contains other groups").build() );
                return 0;
            }

            logger.debug( "saving {}", item );
            currentStatus = "Adding: " + item;
            Item saved = service.saveItem( item );
            item.setId( saved.getId() );
            item.setSkuId( saved.getSkuId() );
            logger.debug( "saving {} with id {}", item, item.getId() );

            // load filters
            List<ItemFilter> filters = getItemAttributes( (ImportedItem)item );
            service.saveItemFilters( filters );

        }


        // send the images to the server, item and group
        for ( Item item : items ) {

            for ( ItemImage img : item.getImages() ) {

                logger.debug( "Sending image {} file {}",
                                img.getImagePath(), img.getLocalSystemPath() );
                byte[] bytes =
                        FileUtil.getFileBytes( img.getLocalSystemPath() );

                if ( !service.imageExists( "", img.getImagePath() ) )

                    // service.sendImage( "", img.getImagePath(), bytes );
                    service.sendImage( item.getId(), item.getName(), 'P',
                            img.getImagePath(), bytes, 'Y');

            }

        }

        for ( ItemGroup grp : groupCache.values() ) {
            if ( grp.getId() == null ||
                                grp.getId().longValue() > 0 ||
                                            grp.getImage().length() == 0 )
                continue;

            logger.debug( "Sending group file {}", grp.getImageSystemLocalPath() );

            byte[] bytes =
                FileUtil.getFileBytes( grp.getImageSystemLocalPath() );

            if ( !service.imageExists( "", grp.getImage() ) ) {
                logger.debug( "Group file exists", grp.getImage() );
            }

            //service.sendImage( grp.getId(), grp.getName(), 'C',
            //        grp.getImage(), bytes, 'Y');

        }



        // add in related items
        for ( Item item : items ) {
            logger.debug( "Saving related items for {}", item  );
            currentStatus = "Adding Related Items to : " + item;
            setRelatedItems( (ImportedItem)item );
            Item saved = service.saveItem( item );
            logger.debug( "saved {} related items",
                                        item.getRelatedItems().size() );
        }

        return 0;
    }



    @SuppressWarnings("unchecked")
    public ImportResult addToCatalog ( Item item ) {

        problems.clear();
        messages.clear();


        if ( !isGroupSaved( item.getGroup() ) ) {
            // save the group
            logger.debug( "saving group {}", item.getGroup() );
            saveGroup ( item.getGroup() );
            logger.debug( "group {} Id is now {}", item.getGroup(), item.getGroup().getId() );
        }

        // check if group has items attached
        if ( service.findAllChildCategories(
                        item.getGroup().getId() ).size() > 0 ) {
            problems.add( new ImportProblem.Builder().
                    item( item ).
                    message( "Assigned item group can not be used " +
                            "because it contains other groups").build() );

            ImportResult rslt = new ImportResult();
            rslt.item = item;
            rslt.message = "Assigned item group can not be used " +
                                        "because it contains other groups";
            rslt.success = false;

            return rslt;
        }

        logger.debug( "saving {}", item );
        currentStatus = "Adding: " + item;
        Item saved = service.saveItem( item );
        System.out.println("Saved item's get id -> "+saved.getId()+" Sku Id -> "+saved.getSkuId()+" Saved Sku -> "+saved.getSku());
        item.setId( saved.getId() );
        item.setSkuId( saved.getSkuId() );
        logger.debug( "saving {} with id {}", item, item.getId() );

        // load filters
        List<ItemFilter> filters = getItemAttributes( (ImportedItem)item );
        service.saveItemFilters( filters );

        ImportResult rslt = new ImportResult();

        /*
         * Implementation for updating the price and inventory for each item added
         */
        updateInventory(item);
        Item itemToBeDeleted = updatePricing(item);
        if(itemToBeDeleted != null) {

            rslt.item = itemToBeDeleted;
            rslt.message = "Discontinued Item .. deleting";
            rslt.success = false;
            service.removeItem(itemToBeDeleted);


        }

        // send the images to the server, item and group

        if(item != null) {
            for ( ItemImage img : item.getImages() ) {

                logger.debug( "Sending image {} file {}",
                                img.getImagePath(), img.getLocalSystemPath() );
                byte[] bytes =
                        FileUtil.getFileBytes( img.getLocalSystemPath() );
                if(bytes.length == 0) {
                	rslt.message = "Missing "+img.getLocalSystemPath();
                    rslt.success = false;
                    return rslt;

                }

                /*
                 * @Check on max image size.
                 * The max value is collected from logs.
                 */
                if(bytes.length > 262144) {
                    rslt.message = "Image size is exceeding the limit";
                    rslt.success = false;
                    return rslt;

                }

                if ( !service.imageExists( "", img.getImagePath() ) )
                    service.sendImage( item.getId(), img.getKey(), 'P',
                            img.getImagePath(), bytes,'Y');
            }
        }



        for ( ItemGroup grp : groupCache.values() ) {

            if(grp != null) {
                /*
                 * Fix null pointer exception on getImage
                 */
                int grp_getImage_lenght = 0;
                if(grp.getImage() != null) {
                    grp_getImage_lenght = grp.getImage().length();
                }
                if ( grp.getId() == null || grp.getId().longValue() > 0 || grp_getImage_lenght == 0 ) {
                    continue;
                }


                logger.debug( "Sending group file {}", grp.getImageSystemLocalPath() );

                byte[] bytes =  FileUtil.getFileBytes( grp.getImageSystemLocalPath() );

                if ( !service.imageExists( "", grp.getImage() ) ) {

                    //service.sendImage( grp.getId(), grp.getName(), 'C',
                    //        grp.getImage(), bytes, 'Y');
                }
            }
        }

        if(item != null) {
            // add in related items
            logger.debug( "Saving related items for {}", item  );
            currentStatus = "Adding Related Items to : " + item;
            setRelatedItems( (ImportedItem)item );
            Item savedItem = service.saveItem( item );
            logger.debug( "saved {} related items",
                                        item.getRelatedItems().size() );

            //ImportResult rslt = new ImportResult();
            rslt.item = savedItem;
            rslt.message = "";
            rslt.success = true;
        }
        return rslt;

    }


    private Item updatePricing(Item item) {
        // TODO Auto-generated method stub
        Item rItm = AS400repository.findItem((item.getSku()).toString());
        if(rItm == null) {
            System.out.println("Item "+item.getSku().toString()+" not found in repository, deleting");
            return item;
        }
        /*if(rItm != null) {
            if ( (rItm.getRegularPrice().doubleValue() != item.getRegularPrice().doubleValue())) {
                item.setRegularPrice(rItm.getRegularPrice());
                if(rItm.getSalePrice().doubleValue() == 0) {
                    item.setSalePrice(rItm.getRegularPrice());
                } else {
                    item.setSalePrice(rItm.getSalePrice());
                }
                //service.saveItem ( item );
             }
        }*/
        if(rItm != null) {
            if ( (rItm.getRegularPrice().doubleValue() != item.getRegularPrice().doubleValue()) ||

                    ((rItm.getSalePrice().doubleValue() != item.getSalePrice().doubleValue()) &&  rItm.getSalePrice().doubleValue() != 0 ) ||

                    ((rItm.getRegularPrice().doubleValue() != item.getSalePrice().doubleValue()) &&  (rItm.getSalePrice().doubleValue() == 0)  )) {

                        item.setRegularPrice(rItm.getRegularPrice());

                        System.out.println("...Updating regular price on item " + item.getName() + " to " + rItm.getRegularPrice());

                       if(rItm.getSalePrice().doubleValue() == 0) {

                           item.setSalePrice(rItm.getRegularPrice());

                          System.out.println("...set sale price to regular price " + item.getName() + " to " + rItm.getRegularPrice());

                       } else {

                           item.setSalePrice(rItm.getSalePrice());

                          System.out.println("...set sale price on item " + item.getName() + " to " + rItm.getSalePrice());

                      }

                      //service.saveItem ( itm );

              }
        }

        return null;
    }


    /*private void updateInventory(Item item) {
        // TODO Auto-generated method stub
        List<ItemInventory> invItems = AS400repository.findItemInventory((item.getSku()).toString());
        if(invItems == null) {
            System.out.println("Item inventory for "+item.getSku()+ " not found in the repository");
        }
        if(invItems != null) {
            System.out.println(" invItems.size() "+invItems.size());
            for(ItemInventory invItm : invItems) {

                if(invItm != null) {
                    invItm.setSkuId(item.getSkuId());
                }
            }
            service.saveItemInventory(invItems);
            System.out.println("...Saved inventory item -> "+item.getName());
        }
        //AS400repository.disconnect();

    }*/

    private void updateInventory(Item item) {
        List<ItemInventory> invItems = AS400repository.findItemInventory((item.getSku()).toString());
        if(invItems == null || invItems.isEmpty()) {
            System.out.println("Removing item inventory for "+item.getSku()+ " since not found in the repository");
            service.removeItem(item);
        }

        if(invItems != null) {
            for(ItemInventory invItm : invItems) {

                if(invItm != null) {
                    invItm.setSkuId(item.getSkuId());
                }
            }
            int value = service.saveItemInventory(invItems);
            if(value == 0) {
                System.out.println("No inventory items to be saved for "+item.getName());
            } else {
                System.out.println("..Saved inventory item -> "+item.getName());
            }
        }

    }


    @SuppressWarnings("unchecked")
    public ImportResult replaceItem ( Item item ) {

        removeItem(item);

        return addToCatalog( item );


    }

    @SuppressWarnings("unchecked")
    public boolean removeItem ( Item item ) {
        // does the item already exists in the catalog?
        Item foundItem =
                    service.findItemBySkuNum( item.getSku().toString() );

        if ( foundItem != null ) {
            service.removeItem( foundItem );
            return true;
        }

        return false;


    }


    public ImportResult precheck ( ImportedItem item ) {

        String message = "";
        ImportResult rslt = new ImportResult ( );

        currentStatus = "Prechecking: " + item.toString();
        logger.debug( currentStatus );


        // required fields present, lengths ok, etc
        InvalidValue[] invalids = itemValidator.getInvalidValues( item );
        if ( invalids.length > 0 ) {
            logger.debug( "{} has problems, {}", item, invalids.length );


            for ( InvalidValue iv : invalids ) {
                problems.add( new ImportProblem.Builder().
                              item( item ).
                              message( "'" + iv.getPropertyName() + "': " +
                                               iv.getMessage() ).build() );
                message += "\n'" + iv.getPropertyName() + "': " + iv.getMessage();
            }

            rslt.message = message;
            rslt.success = false;
            rslt.item = item;

            return rslt;
        }



        // build the correct item group
        logger.debug( "Building item group chain" );
        item.setGroup( buildGroup ( item.getDeliminatedGroupPath(), item.getGroupImage() ) );

        // set item filter/attribute


        // setup appropriate defaults
        item.defaultDates();
        item.setRelatedItems( new ArrayList<Item>(0) );

        // get item images
        logger.debug( "Setting item images" );
        item.setImages( findItemImages( item ) );



        // scan groupCache for groups that have null id,
        // create message for each
        for ( ItemGroup grp : groupCache.values() ) {
            if ( grp.getId() == null || grp.getId().longValue() == -1 )
                logger.debug( "{} will be created", grp );
                messages.add( new ImportInfo.Builder().
                message( "'" + grp.toString() + "' will be created" ).build() );
        }



        // does the item already exists in the catalog?
        Item foundItem =
                    service.findItemBySkuNum( item.getSku().toString() );
        if ( foundItem != null ) {
            // add an error message, remove from items list
            problems.add(
                    new ImportProblem.Builder().item( item ).
                                    message( ITEM_EXISTS ).build() );
            rslt.message = ITEM_EXISTS+SMALL_IMAGE+LARGE_IMAGE+LARGE_SMALL;
            rslt.success = false;
            rslt.item = foundItem;
            SMALL_IMAGE = "";
            LARGE_IMAGE = "";
            LARGE_SMALL = "";
            return rslt;
        }

        rslt.message = message+SMALL_IMAGE+LARGE_IMAGE+LARGE_SMALL;
        rslt.success = true;
        rslt.item = item;
        SMALL_IMAGE = "";
        LARGE_IMAGE = "";
        LARGE_SMALL = "";
        return rslt;
    }



    public int precheck ( List<ImportedItem> items ) {

        List<Item> removeIndexes = new ArrayList<Item>();

        // loop through items and expand information
        int index = -1;
        for ( ImportedItem item : items ) {
            index++;

            currentStatus = "Prechecking: " + item.toString();
            logger.debug( currentStatus );

            // does the item already exists in the catalog?
            Item foundItem =
                        service.findItemBySkuNum( item.getSku().toString() );
            if ( foundItem != null ) {
                // add an error message, remove from items list
                problems.add(
                        new ImportProblem.Builder().item( item ).
                                        message( ITEM_EXISTS ).build() );
                //items.remove( item );
                removeIndexes.add( item );
                continue;
            }

            // required fields present, lengths ok, etc
            InvalidValue[] invalids = itemValidator.getInvalidValues( item );
            if ( invalids.length > 0 ) {
                logger.debug( "{} has problems, {}", item, invalids.length );
                //items.remove( item );
                removeIndexes.add( item );

                for ( InvalidValue iv : invalids ) {
                    problems.add( new ImportProblem.Builder().
                                  item( item ).
                                  message( "'" + iv.getPropertyName() + "': " +
                                                   iv.getMessage() ).build() );
                }

                continue;
            }



            // build the correct item group
            logger.debug( "Building item group chain" );
            item.setGroup( buildGroup ( item.getDeliminatedGroupPath(), item.getGroupImage() ) );

            // set item filter/attribute


            // setup appropriate defaults
            item.defaultDates();
            item.setRelatedItems( new ArrayList<Item>(0) );

            // get item images
            logger.debug( "Setting item images" );
            item.setImages( findItemImages( item ) );

        }

        // scan groupCache for groups that have null id,
        // create message for each
        for ( ItemGroup grp : groupCache.values() ) {
            if ( grp.getId() == null || grp.getId().longValue() == -1 )
                logger.debug( "{} will be created", grp );
                messages.add( new ImportInfo.Builder().
                message( "'" + grp.toString() + "' will be created" ).build() );
        }


        // remove the bad items
        for ( Item i : removeIndexes ) {
            items.remove( i );
        }

        return items.size();
    }


    private ItemGroup buildGroup ( String groupPath, String groupImage ) {

        if ( groupPath == null || groupPath.length() == 0 )
            return null;

        String partialTkn = "";
        long parentId = 1; // start with root parent id

        // tokenize the group
        String[] groupTkn = StringUtils.split( groupPath, '|' );
        ItemGroup[] itmGroupParents = new ItemGroup[groupTkn.length];

        int index = 0;
        for ( String tkn : groupTkn ) {
            tkn = tkn.trim();
            logger.debug( "Group token: {}", tkn );
            partialTkn = "|" + tkn;

            // try cache first
            if ( groupCache.get( partialTkn ) != null ) {
                logger.debug( "group cache hit on {}", partialTkn );
                itmGroupParents[index] = groupCache.get( partialTkn );
                parentId = itmGroupParents[index].getId() == null ?
                                           -1 : itmGroupParents[index].getId();
                index++;
                continue;
            } else { // try to find group already in catalog
                ItemGroup grp = findItemGroupByName( parentId, tkn );

                if ( grp != null ) {
                    logger.debug( "catalog contains group, {}", grp );
                    itmGroupParents[index] = grp;
                    itmGroupParents[index].setDelimintedPath( partialTkn );
                    parentId = grp.getId();

                    groupCache.put( partialTkn, itmGroupParents[index] );

                    index++;
                    continue;
                }
            }

            // not luck finding existing category, must be new
            logger.debug( "new group discovered, {}", tkn );
            ItemGroup newGroup = new ItemGroup.Builder().
                                                id( null ).
                                                name( tkn ).build();
            newGroup.defaultDates();
            newGroup.setDelimintedPath( partialTkn );

            // get group image
            setGroupImage( newGroup, groupImage );

            itmGroupParents[index] = newGroup;
            index++;

            groupCache.put( partialTkn, newGroup );
            parentId = -1;

        }

        // link each group to the parent ahead of it if id present
        for ( int i = itmGroupParents.length - 1; i >= 0; i-- ) {

            if ( i == 0 ) {
                ItemGroup root = new ItemGroup.Builder().
                                    id( 1l ).name( "store" ).build();
                itmGroupParents[i].setParent( root );
                logger.debug( "linked {} to parent {}",
                                                itmGroupParents[i], root );
                continue;
            }

            logger.debug( "linked {} to parent {}", itmGroupParents[i],
                                                    itmGroupParents[i-1] );

            itmGroupParents[i].setParent( itmGroupParents[i-1] );
        }


        // return the last group, now linked to parents
        return itmGroupParents[itmGroupParents.length - 1];
    }



    private ItemGroup findItemGroupByName ( long parentId, String name ) {
        List<ItemGroup> groups = service.findAllChildCategories( parentId );

        if ( groups == null || parentId == -1 )
            return null;

        for ( ItemGroup grp : groups ) {
            if ( grp.getName().equals( name ) )
                return grp;
        }

        return null;
    }

    private boolean isGroupSaved ( ItemGroup group  ) {
        if ( group == null )
            return false;

        if ( group.getId() == null || group.getId().longValue() <= 0 )
            return false;


        return true;
    }

    private List<ItemImage> findItemImages ( ImportedItem item ) {
        List<ItemImage> images = new ArrayList<ItemImage>();

        String sku = item.getSku().toString();
        String model = item.getModelNum();
        File smallImageDir = new File ( IMG_DIR + "/small" );
        File largeImageDir = new File ( IMG_DIR + "/large" );

        NameFileFilter filter = new NameFileFilter (
              new String[] {
               sku+".gif", sku+"A.gif", sku+"B.gif", sku+"C.gif", sku+"D.gif",
               sku+".jpg", sku+"A.jpg", sku+"B.jpg", sku+"C.jpg", sku+"D.jpg",
               sku+".png", sku+"A.png", sku+"B.png", sku+"C.png", sku+"D.png",
               model+".gif", model+"A.gif", model+"B.gif", model+"C.gif", model+"D.gif",
               model+".jpg", model+"A.jpg", model+"B.jpg", model+"C.jpg", model+"D.jpg",
               model+".png", model+"A.png", model+"B.png", model+"C.png", model+"D.png"},
               IOCase.INSENSITIVE );

        //Check if images are existing before sending to the server
        String[] img = smallImageDir.list( filter );

        String[] imgL = largeImageDir.list( filter );

        if((img == null)) {
            SMALL_IMAGE = " Image "+ sku +" is not in "+smallImageDir+".";
        }
        if((imgL == null)) {
            LARGE_IMAGE = " Image "+ sku +" is not in "+largeImageDir+".";
        }

        //logger.debug( "images found {}", img.length );

        for ( String fileName : img ) {
            logger.debug( "Found image for {} -- {}", item, fileName );
            String key = "";

            File file = new File( IMG_DIR + "/small/" + fileName);


            // initialize the key
            if ( fileName.length() > sku.length() || fileName.length() > model.length() ) { // need last char for key
                char index = fileName.charAt( fileName.length() - 1 );

                switch ( index ) {
                    case 'a':
                    case 'A':
                        key = "1";
                        break;
                    case 'b':
                    case 'B':
                        key = "2";
                        break;
                    case 'c':
                    case 'C':
                        key = "3";
                        break;
                    case 'd':
                    case 'D':
                        key = "4";
                        break;
                    default:
                        key = "";
                }
            }


            // checking for matching large image
            File largeImage = new File ( IMG_DIR + "/large/" + fileName );
            if ( !largeImage.exists() ) { // error
                problems.add( new ImportProblem.Builder().
                            item( item ).
                            message( "Missing large image '" + fileName + "'").
                            build() );
             continue; // skip this image
            }


            ItemImage itemImage = new ItemImage();
            itemImage.setKey( "small" + key );
            itemImage.setImagePath( SERVER_IMG_PATH_SMALL + fileName );
            itemImage.setLocalSystemPath( file.getPath() );
            logger.debug( "small local path " + file.getPath() );

            ItemImage largeItemImage = new ItemImage ( );
            largeItemImage.setKey( "large" + key );
            largeItemImage.setImagePath( SERVER_IMG_PATH_LARGE + fileName );
            largeItemImage.setLocalSystemPath( largeImage.getPath() );
            logger.debug( "large local path " + largeImage.getPath() );

            images.add( itemImage );
            images.add( largeItemImage );

        }

        if ( images.size() == 0 ) {
            messages.add(
                    new ImportInfo.Builder().
                        item( item ).
                        message( "No images will be attached" ).build() );
        }

        return images;
    }

    private void setGroupImage ( ItemGroup group, String groupImg ) {

        if ( groupImg == null || groupImg.length() == 0 ) {
            group.setImage( "" );

            messages.add( new ImportInfo.Builder().
                 message( "You did not pick a group image for " +
                                     group.getName() ).build() );

            return;
        }

        File smallImage = new File ( IMG_DIR + "/category/" + groupImg );

        if ( !smallImage.exists() ) {

            group.setImage( "" );
            group.setImageSystemLocalPath( "" );
            messages.add( new ImportInfo.Builder().
                 message( "The image does not exists for " +
                                     group.getDelimintedPath() ).build() );

            return;

        }

        group.setImage( SERVER_IMG_PATH_CATEGORY + smallImage.getName() );
        logger.debug( "group image path {}", smallImage.getPath() );
        group.setImageSystemLocalPath( smallImage.getPath() );

    }

    private void saveGroup ( ItemGroup group ) {
        // a null parent would be the root parent
        logger.debug( "Evaluating {} with parent {}", group, group.getParent() );

        if ( !isGroupSaved( group.getParent() ) ) {

            saveGroup ( group.getParent() );

            logger.debug( "persisting end of chain {}", group );
            group.setId( new Long ( 0 ) ); // ensure the service creates it
            ItemGroup saved = service.saveItemGroup( group );
            group.setId( saved.getId() );
            logger.debug( "persisted end of chain {} with id {}", group, saved.getId() );

        } else {

            logger.debug( "persisting {}", group );
            group.setId( new Long ( 0 ) ); // ensure the service creates it
            ItemGroup saved = service.saveItemGroup( group );
            group.setId( saved.getId() );
            logger.debug( "persisted {} with id {}", group, saved.getId() );

        }

    }


    private List<ItemFilter> getItemAttributes ( ImportedItem item ) {

        List<ItemFilter> filters = new ArrayList<ItemFilter>(4);

        if ( item.getFilter1Name() != null && item.getFilter1Value() != null &&
                    item.getFilter1Name().length() > 0 &&
                    item.getFilter1Value().length() > 0 ) {
            ItemFilter filter = new ItemFilter.Builder().
                            itemId( item.getSkuId() ).
                            name( item.getFilter1Name() ).
                            value( item.getFilter1Value() ).build();

            filters.add( filter );
        }

        if ( item.getFilter2Name() != null && item.getFilter2Value() != null &&
                item.getFilter2Name().length() > 0 &&
                item.getFilter2Value().length() > 0 ) {
            ItemFilter filter = new ItemFilter.Builder().
                    itemId( item.getSkuId() ).
                    name( item.getFilter2Name() ).
                    value( item.getFilter2Value() ).build();

            filters.add( filter );
        }

        if ( item.getFilter3Name() != null && item.getFilter3Value() != null &&
                item.getFilter3Name().length() > 0 &&
                item.getFilter3Value().length() > 0 ) {
            ItemFilter filter = new ItemFilter.Builder().
                    itemId( item.getSkuId() ).
                    name( item.getFilter3Name() ).
                    value( item.getFilter3Value() ).build();

            filters.add( filter );
        }

        if ( item.getFilter4Name() != null && item.getFilter4Value() != null &&
                item.getFilter4Name().length() > 0 &&
                item.getFilter4Value().length() > 0 ) {
            ItemFilter filter = new ItemFilter.Builder().
                    itemId( item.getSkuId() ).
                    name( item.getFilter4Name() ).
                    value( item.getFilter4Value() ).build();

            filters.add( filter );
        }

        return filters;
    }


    private void setRelatedItems ( ImportedItem item ) {

        item.setRelatedItems( new ArrayList<Item>() );

        if ( item.getDeliminatedRelatedItems() == null ||
                        item.getDeliminatedRelatedItems().length() == 0 )
            return;

        String[] relatedSkuNums =
                StringUtils.split( item.getDeliminatedRelatedItems(), ',' );

        for ( String skuNum : relatedSkuNums ) {

            Item skuItem = service.findItemBySkuNum( skuNum.trim() );

            if ( skuItem == null ) {
                problems.add( new ImportProblem.Builder().
                   item( item ).
                   message(
                        "Related SKU, " + skuNum + " is not in the catalog").
                   build() );

                continue;
            }


            item.getRelatedItems().add( skuItem );

        }
    }


    public List<ImportProblem> getProblems() {
        return problems;
    }

    public void setProblems(List<ImportProblem> problems) {
        this.problems = problems;
    }

    public List<ImportInfo> getMessages() {
        return messages;
    }

    public void setMessages(List<ImportInfo> messages) {
        this.messages = messages;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public class ImportResult {
        public Item item;
        public String message = "";
        public boolean success = false;
    }

}
