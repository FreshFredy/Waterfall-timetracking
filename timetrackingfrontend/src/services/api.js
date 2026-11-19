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

    getTeamTimesheets(supervisorId, status) {
    return http.get('/Timesheet-management/Timesheets/team', { params: { supervisorId, status } })
    }
}


class SickLeaveDataService {
    
    get(id) {
        return http.get(`/SickLeave-management/SickLeaves/${id}`);
    }

    // Angepasst an deinen Backend-Pfad mit /employee/{employeeId}
    getByEmployee(employeeId) {
        return http.get(`/SickLeave-management/SickLeaves/employee/${employeeId}`);
    }

    recordSickLeave(hrId, data) {
        return http.post(`/SickLeave-management/SickLeaves?hrId=${hrId}`, data);
    }
}

class VacationRequestDataService {

    // Entspricht: @GetMapping("/VacationRequests") mit @RequestParam employeeId
    getRequestsOfEmployee(employeeId) {
        return http.get(`/VacationRequest-management/VacationRequests?employeeId=${employeeId}`);
    }

    // Entspricht: @GetMapping("/VacationRequests/pending") mit @RequestParam supervisorId
    getPending(supervisorId) {
        return http.get(`/VacationRequest-management/VacationRequests/pending?supervisorId=${supervisorId}`);
    }

    // Entspricht: @GetMapping("/VacationRequests/{id}")
    getRequest(id) {
        return http.get(`/VacationRequest-management/VacationRequests/${id}`);
    }

    // Entspricht: @PostMapping("/VacationRequests") mit @RequestParam employeeId und @RequestBody
    create(employeeId, data) {
        return http.post(`/VacationRequest-management/VacationRequests?employeeId=${employeeId}`, data);
    }

    // Entspricht: @PostMapping("/VacationRequests/{id}/approve") mit @RequestParam supervisorId
    approve(id, supervisorId) {
        return http.post(`/VacationRequest-management/VacationRequests/${id}/approve?supervisorId=${supervisorId}`);
    }

    // Entspricht: @PostMapping("/VacationRequests/{id}/reject") mit @RequestParam und @RequestBody { reason }
    reject(id, supervisorId, reason) {
        return http.post(`/VacationRequest-management/VacationRequests/${id}/reject?supervisorId=${supervisorId}`, { reason });
    }

    // Entspricht: @PostMapping("/VacationRequests/{id}/cancel") mit @RequestParam employeeId
    cancel(id, employeeId) {
        return http.post(`/VacationRequest-management/VacationRequests/${id}/cancel?employeeId=${employeeId}`);
    }
}

class NotificationDataService {

    // Entspricht: @GetMapping("/Notifications") mit employeeId und optionalem unreadOnly
    getNotifications(employeeId, unreadOnly = false) {
        return http.get(`/Notification-management/Notifications?employeeId=${employeeId}&unreadOnly=${unreadOnly}`);
    }

    // Entspricht: @GetMapping("/Notifications/unread-count")
    countUnread(employeeId) {
        return http.get(`/Notification-management/Notifications/unread-count?employeeId=${employeeId}`);
    }

    // Entspricht: @PostMapping("/Notifications/{id}/read")
    markAsRead(id, employeeId) {
        return http.post(`/Notification-management/Notifications/${id}/read?employeeId=${employeeId}`);
    }

    // Entspricht: @PostMapping("/Notification-management/Notifications/read-all")
    markAllAsRead(employeeId) {
        return http.post(`/Notification-management/Notifications/read-all?employeeId=${employeeId}`);
    }
}



export const notificationService = new NotificationDataService();
export const vacationRequestService = new VacationRequestDataService();
export const sickLeaveService = new SickLeaveDataService();
export const timesheetService = new TimesheetDataService();
export default new EmployeeDataService();