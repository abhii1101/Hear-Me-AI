import axios from 'axios';
const API = {
  GetChatbotResponse: async message => {
    /*return new Promise(async function(resolve, reject) {
      if (message === "hi") 
        resolve("Welcome to chatbot!");
      else {
        debugger;
        const response = await axios.get('http://localhost:8080/api/chatbot/send?message='+message);
        if (response.status === 200){
          resolve(response.data);
        }
        else{
          reject("Chatbot service unavailable. Please try again later.");
        }
      }
    });*/

    try{
      let returnmessage="";
      if (message === "hi") {
        returnmessage="Welcome to chatbot!";
        //resolve(returnmessage);
      }
      else {
        //debugger;
        const response = await axios.post('http://localhost:8080/api/chatbot/send',message);
        if (response.status === 200){
          returnmessage=response.data;
          //resolve(response.data);
        }
        else{
          returnmessage="Chatbot service unavailable. Please try again later.";
          //reject("Chatbot service unavailable. Please try again later.");
        }
      }
      return returnmessage;
    }
    catch(error){
      console.error("Error:"+error);
    }
  }
};

export default API;






