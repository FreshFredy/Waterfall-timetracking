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

class TimesheetDataService {
    getTimesheets(employeeId, month = null, year = null) {
        let url = `/Timesheet-management/Timesheets?employeeId=${employeeId}`;
        if (month !== null && month !== undefined) url += `&month=${month}`;
        if (year !== null && year !== undefined) url += `&year=${year}`;
        return http.get(url);
    }

    getTimesheet(id) {
        return http.get(`/Timesheet-management/Timesheets/${id}`);
    }

    recordWorkTime(employeeId, data) {
        return http.post(`/Timesheet-management/Timesheets/entries?employeeId=${employeeId}`, data);
    }

    changeWorkTime(timesheetId, entryId, data) {
        return http.put(`/Timesheet-management/Timesheets/${timesheetId}/entries/${entryId}`, data);
    }

    submit(id) {
        return http.post(`/Timesheet-management/Timesheets/${id}/submit`);
    }

    approve(id, supervisorId) {
        return http.post(`/Timesheet-management/Timesheets/${id}/approve?supervisorId=${supervisorId}`);
    }

    reject(id, supervisorId, reason) {
        return http.post(`/Timesheet-management/Timesheets/${id}/reject?supervisorId=${supervisorId}`, { reason });
    }
}

export const timesheetService = new TimesheetDataService();
export default new EmployeeDataService();