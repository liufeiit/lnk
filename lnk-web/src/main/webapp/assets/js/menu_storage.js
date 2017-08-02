/**
 * 刘飞
 */
$(document).ready(function() {
	if (window.localStorage) {
        var current_menu = window.localStorage.getItem('current_menu');
        if (current_menu != '' && current_menu != null) {
            $('#' + current_menu).addClass("active");
        }
        var current_sub_menu = window.localStorage.getItem('current_sub_menu');
        if (current_sub_menu != '' && current_sub_menu != null) {
            $('#' + current_sub_menu).addClass("active");
        }
    }
    $('#homePage').click(function() {
        window.localStorage.setItem("current_menu", '');
        window.localStorage.setItem("current_sub_menu", '');
    });
    $('.treeview-menu a').click(function() {
        if (window.localStorage) {
            var current_menu = $(this).parent().parent().parent().attr('id');
            window.localStorage.setItem("current_menu", current_menu);
            var current_sub_menu = $(this).parent().attr('id');
            window.localStorage.setItem("current_sub_menu", current_sub_menu);
        }
    });
});