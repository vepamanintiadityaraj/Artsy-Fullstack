const jwt = require("jsonwebtoken");

const authenticate =(req, res,next) => {
    const token = req.cookies.token;
    if(!token){return res.status(401).json({error: "No token available "});}
    try{
        const check = jwt.verify(token, "QWErty123");
        req.user = check;
        next();
    }
    catch(error){
        console.error("Middleware Error:", error);
        res.status(403).json({error: "Middleware error: Token expired "});
       }
};

module.exports = authenticate;