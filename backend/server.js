
const express = require('express');
const axios = require("axios");
const app = express();
const mongo=require("mongoose");
const artsyAccount = require("./Database/mangoosetest.js");
const authenticateUser = require("./middleware/authentication.js");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");
const cookieParser = require("cookie-parser");
const crypto = require("crypto");
const cors = require("cors");
const path = require("path");



const port = process.env.PORT || 3000;

app.use(cookieParser());
app.use(express.json());
app.use(cors());

const Token_Key= "QWErty123";

async function gettoken() {
    const url = "https://api.artsy.net/api/tokens/xapp_token";
    const fs = require("fs").promises;
    const data = await fs.readFile("credentials.json","utf8");
    const creds=JSON.parse(data);
    const response = await axios.post(url, creds);
    return response.data.token;  
}
function gravatar(email){
  const hash= crypto.createHash("sha1").update(email.trim().toLowerCase()).digest("hex");
  return `https://www.gravatar.com/avatar/${hash}?s=200&d=identicon`;
}


const gen_token = (id) =>{
  return jwt.sign({ id }, "QWErty123", { expiresIn: "1h" });
}



app.get("/api/search/:artistName", async(req,res)=>{
  try{
  const artistName = req.params.artistName;
  const artsyToken = await gettoken()
  const resp= await axios.get("https://api.artsy.net/api/search",{
    headers:{
      "X-XAPP-Token": artsyToken
    },
    params: {
      q: artistName,
      type : "artist",
      size : 10
    }
  });

  
    res.json(resp.data);
  }
  catch(error){
    res.status(500).json({error: "Artist search error"});
  }
  });
  
  app.get("/api/end/:id", async(req,res)=>{
    try{
    const artistId = req.params.id;
    const artsyToken = await gettoken()
    const resp= await axios.get(`https://api.artsy.net/api/artists/${artistId}`,{
      headers:{
        "X-XAPP-Token": artsyToken
      },
      
    });
  
    
      res.json(resp.data);
    }
    catch(error){
      console.log(error)
      res.status(500).json({error: "Artist bio error"});
    }
    });

    app.get("/api/artworks/:id", async(req,res)=>{
      try{
      const artistId = req.params.id;
      const artsyToken = await gettoken()
      const resp= await axios.get("https://api.artsy.net/api/artworks",{
        headers:{
          "X-XAPP-Token": artsyToken
        },
        params: {
          artist_id: artistId,
          size : 10
        }
        
      });
    
      
        res.json(resp.data);
      }
      catch(error){
        res.status(500).json({error: "Artist artworks error"});
      }
      });


      app.get("/api/artists/:id", async(req,res)=>{
        try{
        const artistId = req.params.id;
        const artsyToken = await gettoken()
        const resp= await axios.get("https://api.artsy.net/api/artists",{
          headers:{
            "X-XAPP-Token": artsyToken
          },
          params: {
            similar_to_artist_id: artistId,
            size : 10
          }
          
        });
      
        
          res.json(resp.data);
        }
        catch(error){
          res.status(500).json({error: "Similar artists error"});
        }
        });

      app.get("/api/genes/:id", async(req,res)=>{
        try{
        const artworkId = req.params.id;
        const artsyToken = await gettoken()
        const resp= await axios.get("https://api.artsy.net/api/genes",{
          headers:{
            "X-XAPP-Token": artsyToken
          },
          params: {
            artwork_id: artworkId,
          }
          
        });
      
        
          res.json(resp.data);
        }
        catch(error){
          res.status(500).json({error: "Artist Genes error"});
        }
        });

app.post("/api/reg/", async (req,res)=> {
  try{
     
    const {fullName, email,password } = req.body;
    const user1= await artsyAccount.findOne({email});
    if(user1){
      console.log("Registration Here:", );
      return res.status(400).json({error: "exisisting error"});
    }
    const hashedPass= await bcrypt.hash(password,10);
    const image = gravatar(email);
    const user = new artsyAccount({fullName, email, password : hashedPass, imagelink : image, tokens:[],favorites: {}});
    await user.save();
    
    const token= gen_token(user._id);


    user.tokens.push(token);
    await user.save();


    res.cookie("token", token, { maxAge: 3600000, httpOnly : true });
    res.status(201).json({message: "User added to the database", user:user });

   }
   catch(error){
    console.error("Registration Error:", error);
    res.status(500).json({error: "Server error"});
   }


});


app.post("/api/login/", async (req,res)=> {
  try{
     
    const {email,password } = req.body;
    const user1= await artsyAccount.findOne({email});
    if(!user1){
      console.log("Wrong email", );
      return res.status(400).json({error: "email error"});
    }
    const hashedPass= await bcrypt.compare(password,user1.password);

    if(!hashedPass){
      console.log("Wrong password", );
      return res.status(400).json({error: "password error"});
    }
    const token= gen_token(user1._id);

    user1.tokens.push(token);
    await user1.save();
    res.cookie("token", token, { maxAge: 3600000, httpOnly: true });
    res.status(201).json({message: "Login successful", user:user1 });
    console.log("login sucessful");
   }
   catch(error){
    console.error("Login Error:", error);
    res.status(500).json({error: "Server error"});
   }


});


app.post("/api/logout/", authenticateUser, async (req,res)=> {
  try{
     
    const token= req.cookies.token;
    if(!token){return res.status(401).json({error: "No token "});}
    
    const user1 = await artsyAccount.findOneAndUpdate(
      { tokens: token },
      { $pull: { tokens: token } },  
      { new: true }
  );
  if(!user1){return res.status(401).json({error: "Invalid"});}
  res.clearCookie("token");

    res.status(201).json({message: "Logout successful", token : user1.tokens});

   }
   catch(error){
    console.error("Logout Error:", error);
    res.status(500).json({error: "Logout error"});
   }


});


app.delete("/api/del/", authenticateUser, async (req,res)=> {
  try{
    const id = req.user.id;

    const user1= await artsyAccount.findByIdAndDelete(id);


  if(!user1){return res.status(404).json({error: "User not present"});}
  res.clearCookie("token");

    res.status(200).json({message: "Delete successful", email : user1.email});

   }
   catch(error){
    console.error("Delete Error:", error);
    res.status(500).json({error: "Delete error"});
   }


});


app.get("/api/me", authenticateUser, async (req, res) => {
  try {
    const id = req.user.id;
    const user1 = await artsyAccount.findById(id);

    if (!user1) {
      return res.status(404).json({ error: "User not found" });
    }
    console.log(req.user)
res.status(200).json({
  _id: user1._id,
  fullName: user1.fullName,
  email: user1.email,
  imagelink: user1.imagelink,
  favorites: user1.favorites
});
  } catch (error) {
    console.error("Error in /me route:", error);
    res.status(500).json({ error: "Server error while fetching user info" });
  }
});


app.post("/api/addFav/", authenticateUser, async (req,res)=> {
  try{
    const id = req.user.id;
    const {artist} = req.body;

    const user1= await artsyAccount.findById(id);


  if(!user1){return res.status(404).json({error: "User not present"});}
  user1.favorites[artist.id]={
    name: artist.name,
    birthday: artist.birthday,
    deathday: artist.deathday,
    nationality: artist.nationality,
    image: artist.image,
    DateTimeAdded: new Date().toISOString(),
  };
  user1.markModified("favorites"); 
  await user1.save();


    res.status(200).json({message: "Artist added to favourites", email : user1.email, favorites: user1.favorites,});

   }
   catch(error){
    console.error("Add favourites error:", error);
    res.status(500).json({error: "Favourites adding error"});
   }


});

app.post("/api/remFav/", authenticateUser, async (req,res)=> {
  try{
     
    const id = req.user.id;
    const {artistId} = req.body;
    
    const user1 = await artsyAccount.findById(id);
    if (!user1) return res.status(404).json({ error: "User not found" });

    delete user1.favorites[artistId];
    user1.markModified("favorites"); 
    await user1.save();

  res.status(200).json({message: "Artist removed to favourites", email : user1.email, favorites: user1.favorites,});

   }
   catch(error){
    console.error("Favorites Removal Error:", error);
    res.status(500).json({error: "Favorites Removal error"});
   }


});

app.post("/api/getFav/", authenticateUser, async (req,res)=> {
  try{
     
    const id = req.user.id;
    
    
    const user1 = await artsyAccount.findById(id).select("favorites email");
  if(!user1){return res.status(404).json({error: "User not found"});}
  

  res.status(200).json({message: "Favorites are given as follows", email : user1.email, favorites: user1.favorites,});

   }
   catch(error){
    console.error("Favorites Getting Error:", error);
    res.status(500).json({error: "Favorites Getting error"});
   }


});







app.use(express.static(path.join(__dirname, "dist"))); 

app.get("*", (req, res) => {
    res.sendFile(path.join(__dirname, "dist", "index.html")); 
});

app.listen(port, () => {
    console.log(`Server listening at http://localhost:${port}`);
});

