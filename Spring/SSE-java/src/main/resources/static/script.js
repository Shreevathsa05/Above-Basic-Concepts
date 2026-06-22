console.log("This is script: Page Loaded");

function openConnection(){
    const eventSource = new EventSource("/api/v1/stream");

    eventSource.onmessage=(e)=>{
        console.log(e.data)
    }
}

const button=document.getElementById("connect_button");
button.addEventListener("click", openConnection)