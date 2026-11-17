import http from "./http-common";

class EmployeeDataService {

    getAll(email = null) {
        let url = "/Employee-management/Employees"; // <--- Hier ergänzt
        if (email) {
            url += `?email=${encodeURIComponent(email)}`;
        }
        return http.get(url);
    }

    get(id) {
        return http.get(`/Employee-management/Employees/${id}`); // <--- Hier ergänzt
    }

    getVacationBalance(id) {
        return http.get(`/Employee-management/Employees/${id}/vacation-balance`); // <--- Hier ergänzt
    }

    getFlexAccount(id) {
        return http.get(`/Employee-management/Employees/${id}/flex-account`); // <--- Hier ergänzt
    }

    create(data) {
        return http.post("/Employee-management/Employees", data);
    }

    update(id, data) {
        return http.put(`/Employee-management/Employees/${id}`, data);
    }

    updateVacationBalance(id, data) {
        return http.put(`/Employee-management/Employees/${id}/vacation-balance`, data);
    }

    updateFlexAccount(id, data) {
        return http.put(`/Employee-management/Employees/${id}/flex-account`, data);
    }

    delete(id) {
        return http.delete(`/Employee-management/Employees/${id}`);
    }

    deleteAll() {
        return http.delete(`/Employee-management/Employees`);
    }
}

export default new EmployeeDataService();