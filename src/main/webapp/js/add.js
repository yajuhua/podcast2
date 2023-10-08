function handleEpisodesChange(select) {
    var selectedValue = select.value;
    var customInputContainer = document.getElementById("customInputContainer");

    if (selectedValue === "1") {
        customInputContainer.style.display = "block";
    } else {
        customInputContainer.style.display = "none";
    }
}

window.document.getElementById("btn").onclick = function () {
    let url = window.document.getElementById("url").value;
    let type = window.document.getElementById("select-left").value;
    let frequency = window.document.getElementById("select-right").value;
    let survival = window.document.getElementById("select-survival_time").value;
    let episodes = 0;
    if (window.document.getElementById("select-Episodes").value == "-1") {
        episodes = -1;
    } else if (window.document.getElementById("select-Episodes").value == "1") {
        episodes = document.getElementById("customInput").value;
    }

    axios({
        method: "post",
        url: "./xmlFactoryServlet",
        data: "url=" + url + "&type=" + type + "&frequency=" + frequency + "&survival=" + survival + "&episodes=" + episodes
    }).then(function (resp) {
        if (resp.data === "ok") {
            window.location.href = "index.html";
        } else {
            alert("添加失败！");
        }
    })
}