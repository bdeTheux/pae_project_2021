let navBar = document.querySelector(".navbar");
import {getUserSessionData, removeSessionData} from "../utils/session.js";
import { RedirectUrl } from "./Router.js";
// destructuring assignment
const Navbar = () => {
  let nb;
  let userData = getUserSessionData();
  
  
  if (userData) {
    nb = `
    <h1 class="lines" ></h1>
        <div class= "title">
          <img class="rect-logo" src="assets/rectangle.svg" alt="rectangle logo">
          <img class="logo-writing" src="assets/lvs.svg" alt="logo">
        </div>
        <div id="category" class="dropdown">
          <span class="btn btn-secondary dropdown-toggle condensed small-caps" type="button" id="dropdownMenuButton1" data-bs-toggle="dropdown" aria-expanded="false">
            Types de meuble
          </span>
          <ul class="dropdown-menu condensed" aria-labelledby="dropdownMenuButton1">
            <li><a class="dropdown-item" href="#">Type1</a></li>
            <li><a class="dropdown-item" href="#">Type2</a></li>
            <li><a class="dropdown-item" href="#">Type3</a></li>
          </ul>
        </div>

        <div class="user-head">
          <p class="text-user">Bonjour,</p>
          
          <p id="username" class="text-user">${userData.user.username}</p>
          <div class="dropleft">
            <i id="user" class="bi bi-person-circle dropdown-toggle-user"></i>
            <ul class="dropdown-menu dropdown-menu-left condensed" aria-labelledby="dropdownMenuButton1">
              <li><a class="dropdown-item" id="profile" href="#">Profile</a></li>
              <li><a class="dropdown-item" id="logout" href="#">Se déconnecter</a></li>
            </ul>
          </div>
          <div id="adminToolsIcon">          
          </div>
        </div>
        `;
    navBar.innerHTML = nb;
    console.log(userData.user);
    if (userData){ //just for test, à remplacer avec roler === 'admin'
      console.log("salut");
      let adminTools = document.getElementById("adminToolsIcon");
      adminTools.innerHTML = `<img src="../assets/key4Admin.png" alt="key" id="keyAdmin" width="30" height="30">`;
      let keyAdmin = document.getElementById("keyAdmin");
      keyAdmin.addEventListener("click", onClickTools);
    }
  } else {
    nb = `<h1 class="lines" ></h1>
    <div class= "title">
      <img class="rect-logo" src="assets/rectangle.svg" alt="rectangle logo">
      <img class="logo-writing" src="assets/lvs.svg" alt="logo">
    </div>
    <div id="category" class="dropdown">
      <span class="btn btn-secondary dropdown-toggle condensed small-caps" type="button" id="dropdownMenuButton1" data-bs-toggle="dropdown" aria-expanded="false">
        Types de meuble
      </span>
      <ul class="dropdown-menu condensed" aria-labelledby="dropdownMenuButton1">
        <li><a class="dropdown-item" href="#">Type1</a></li>
        <li><a class="dropdown-item" href="#">Type2</a></li>
        <li><a class="dropdown-item" href="#">Type3</a></li>
      </ul>
    </div>

    <a class="btn btn-dark btn-navbar condensed small-caps" href="#" data-uri="/login">S'identifier</a>`;
    navBar.innerHTML = nb;
  }

  if (userData){
    let logout = document.querySelector("#logout");
    logout.addEventListener("click", onLogout);
  }

};


const onLogout = (e) =>{
  e.preventDefault();
  removeSessionData();
  RedirectUrl("/");
  Navbar();
};

const onClickTools = (e) => {
  e.preventDefault();
  RedirectUrl("/confirmRegistration")
}

export default Navbar;
