    function searchProduct(s){

        if(s == 'no'){
            localStorage.setItem("searchProduct", 'no');
        }else{
             localStorage.setItem("searchProduct", s);
        }
       
       window.location.href = "searchProduct.html";

    }