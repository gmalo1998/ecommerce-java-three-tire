import { useAppSelector } from "../../../../Redux Toolkit/Store";

const TopBrand = () => {
const { homePage } = useAppSelector((store) => store);

return ( <div className="grid gap-4 grid-rows-12 grid-cols-12 lg:h-[600px] px-5 lg:px-20"> <div className="col-span-3 row-span-12 text-white rounded"> <img
       className="w-full h-full object-cover border-fuchsia-800 lg:border-[9px]s rounded-md"
       src={homePage.homePageData?.grid[0].image}
       alt=""
     /> </div>


  <div className="col-span-2 row-span-6 text-white rounded">
    <img
      className="w-full h-full object-cover border-fuchsia-800 lg:border-[9px]s rounded-md"
      src={homePage.homePageData?.grid[1].image}
      alt=""
    />
  </div>

  <div className="col-span-4 row-span-6 text-white rounded">
    <img
      className="w-full h-full object-cover object-top border-fuchsia-800 lg:border-[9px]s rounded-md"
      src={homePage.homePageData?.grid[2].image}
      alt=""
    />
  </div>

  <div className="col-span-3 row-span-12 text-white rounded">
    <img
      className="w-full h-full object-cover object-top border-fuchsia-800 lg:border-[9px]s rounded-md"
      src={homePage.homePageData?.grid[3].image}
      alt=""
    />
  </div>

  <div className="col-span-4 row-span-6 text-white rounded">
    <img
      className="w-full h-full object-cover object-top border-fuchsia-800 lg:border-[9px]s rounded-md"
      src={homePage.homePageData?.grid[4].image}
      alt=""
    />
  </div>

  <div className="col-span-2 row-span-6 text-white rounded">
    <img
      className="w-full h-full object-cover border-fuchsia-800 lg:border-[9px]s rounded-md"
      src={homePage.homePageData?.grid[5].image}
      alt=""
    />
  </div>
</div>


);
};

export default TopBrand;
