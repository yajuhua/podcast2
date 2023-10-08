window.document.getElementById("btn").onclick = function () {
    let username = window.document.getElementById("username").value;
    let password = window.document.getElementById("password").value;

    axios({
        method: "post",
        url: "./loginServlet",
        data: "username=" + username + "&password=" + password
    }).then(function (resp) {
        if (resp.data === "ok") {
            window.location.href = "index.html";
        } else {
            alert(resp.data);
        }
    })
}