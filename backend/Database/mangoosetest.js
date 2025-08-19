const mongo=require("mongoose");

async function connect(){
    await mongo.connect("mongodb://");
    console.log("connected");
}

connect();

const artsySchema= new mongo.Schema({
    fullName : { type : String, required : true } ,
    email : { type : String, required : true, unique : true},
    password : { type : String, required : true } ,
    imagelink: { type : String, required : true },
    tokens : [{ type : String }],
    favorites: {
        type: Object,
        default: {}
      },
});

const artsyAccount = mongo.model("artsyAccount", artsySchema)

module.exports = artsyAccount;