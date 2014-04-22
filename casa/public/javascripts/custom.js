$(document).ready(function() {


	$( ".theme-default" ).click(function() {
	  $( "body" ).removeClass( "theme" );
	  $( "body" ).removeClass( "red-theme" );
	  return false;
	});
    
	$( ".theme-red" ).click(function() {
	  $( "body" ).addClass( "red-theme" );
	  $( "body" ).removeClass( "theme" );
	  return false;
	});
    
	$( ".theme-blue" ).click(function() {
	  $( "body" ).addClass( "theme" );
	  $( "body" ).removeClass( "red-theme" );
	  return false;
	});


});
