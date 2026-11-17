import axios from "axios";

export default axios.create({
    baseURL: "http://localhost:6969/api-timetracking",
    headers: {
        "Content-Type": "application/json"
    }
});