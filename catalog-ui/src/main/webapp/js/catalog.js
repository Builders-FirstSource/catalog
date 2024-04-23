// $(document).ready(function(){
//   $("#catalogSort").change(function () {
//	  document.sortForm.submit();
//   });
// });


function updateSearchFilterResults() {
	var sortValue = $('#catalogSort option:selected').val();
	
    $('#mainContent').prepend("<div class='grayedOut'><img style='margin-top:25px' src='/catalog/images/ajaxLoading.gif'/></div>");
    var postData = $('#refineSearch').serializeArray();
    postData.push({name:'ajax',value:'true'});
    postData.push({name:'catalogSort',value:$('#catalogSort option:selected').val()});
    $('#mainContent').load($('#refineSearch').attr('action'), postData, function () { 
    	// update sort value
        $('#catalogSort').val( sortValue );
    });
    
    
}
