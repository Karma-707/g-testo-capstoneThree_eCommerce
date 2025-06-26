let productService;

class ProductService {

    photos = [];

filter = {
    cat: undefined,
    minPrice: undefined,
    maxPrice: undefined,
    color: undefined,
    search: undefined,      // <-- NEW

    queryString: function() {      // <---- CHANGE here: use function(), not arrow
        let qs = "";
        if(this.cat){ qs = `cat=${this.cat}`; }
        if(this.minPrice)
        {
            const minP = `minPrice=${this.minPrice}`;
            if(qs.length>0) {   qs += `&${minP}`; }
            else { qs = minP; }
        }
        if(this.maxPrice)
        {
            const maxP = `maxPrice=${this.maxPrice}`;
            if(qs.length>0) {   qs += `&${maxP}`; }
            else { qs = maxP; }
        }
        if(this.color)
        {
            const col = `color=${this.color}`;
            if(qs.length>0) {   qs += `&${col}`; }
            else { qs = col; }
        }

        if(this.search) {
            const s = `search=${encodeURIComponent(this.search)}`;
            qs = qs.length > 0 ? `${qs}&${s}` : s;
        }

        return qs.length > 0 ? `?${qs}` : "";
    }
};


    constructor() {

        //load list of photos into memory
        axios.get("/images/products/photos.json")
            .then(response => {
                this.photos = response.data;
            });
    }

    hasPhoto(photo){
        return this.photos.filter(p => p == photo).length > 0;
    }

    addCategoryFilter(cat)
    {
        if(cat == 0) this.clearCategoryFilter();
        else this.filter.cat = cat;
    }
    addMinPriceFilter(price)
    {
        if(price == 0 || price == "") this.clearMinPriceFilter();
        else this.filter.minPrice = price;
    }
    addMaxPriceFilter(price)
    {
        if(price == 0 || price == "") this.clearMaxPriceFilter();
        else this.filter.maxPrice = price;
    }
    addColorFilter(color)
    {
        if(color == "") this.clearColorFilter();
        else this.filter.color = color;
    }

    clearCategoryFilter()
    {
        this.filter.cat = undefined;
    }
    clearMinPriceFilter()
    {
        this.filter.minPrice = undefined;
    }
    clearMaxPriceFilter()
    {
        this.filter.maxPrice = undefined;
    }
    clearColorFilter()
    {
        this.filter.color = undefined;
    }

    addSearchFilter(searchTerm) {
        if(searchTerm === "") this.clearSearchFilter();
        else this.filter.search = searchTerm;
    }

    clearSearchFilter() {
        this.filter.search = undefined;
    }


    search()
    {
        const url = `${config.baseUrl}/products${this.filter.queryString()}`;
        console.log('Request URL:', url);

        axios.get(url)
             .then(response => {
                 let data = {};
                 data.products = response.data;

                 data.products.forEach(product => {
                     if(!this.hasPhoto(product.imageUrl))
                     {
                         product.imageUrl = "no-image.jpg";
                     }
                 })

                 templateBuilder.build('product', data, 'content', this.enableButtons);

             })
            .catch(error => {

                const data = {
                    error: "Searching products failed."
                };

                templateBuilder.append("error", data, "errors")
            });
    }

    enableButtons()
    {
        const buttons = [...document.querySelectorAll(".add-button")];

        if(userService.isLoggedIn())
        {
            buttons.forEach(button => {
                button.classList.remove("invisible")
            });
        }
        else
        {
            buttons.forEach(button => {
                button.classList.add("invisible")
            });
        }
    }

    clearAllFilters() {
        this.clearCategoryFilter();
        this.clearMinPriceFilter();
        this.clearMaxPriceFilter();
        this.clearColorFilter();
        this.clearSearchFilter();
    }

}







document.addEventListener('DOMContentLoaded', () => {
    productService = new ProductService();

});
