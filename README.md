# Development Roadmap

The system skeleton is in place, but every function still needs to be expanded to meet the full set of requirements. The roadmap below breaks the work into phased milestones. Within each phase, tackle the functions in the order listed so that later logic can rely on the earlier guarantees.

## Phase 1 – Core Entities & Data Integrity

**User & subclasses**
- `User#login`, `User#logout`, `User#changePassword`, `User#getUserID`, `User#getName`, `User#setName`, `User#getFilterPreferences`, `User#setFilterPreferences`: wire these to persistence and audit trails, enforce password policies, and make sure filter preferences survive menu switches.
- `Student#getYearOfStudy`, `Student#getMajor`, `Student#getApplications`, `Student#apply`, `Student#withdraw`, `Student#acceptPlacement`, `Student#hasAcceptedPlacement`: enforce year/major constraints, cap three concurrent applications, auto-withdraw conflicting submissions, surface application history even when postings are hidden.
- `CompanyRep#getCompanyName`, `CompanyRep#setCompanyName`, `CompanyRep#createInternship`, `CompanyRep#viewApplications`, `CompanyRep#toggleVisibility`, `CompanyRep#getInternships`, `CompanyRep#isApproved`, `CompanyRep#setApproved`: add limits (max five postings, max ten slots), gather full internship details, gate actions on approval, and expose filtered views of submitted opportunities and applications.
- `CareerCenterStaff#getStaffDepartment`, `CareerCenterStaff#setStaffDepartment`, `CareerCenterStaff#approveInternship`, `CareerCenterStaff#rejectInternship`, `CareerCenterStaff#approveRepAccount`, `CareerCenterStaff#processWithdrawal`: integrate these with controllers/managers so approvals persist, notifications dispatch, and withdrawal decisions update applications and slots appropriately.

**Domain value objects**
- `FilterCriteria` (constructor, `getStatus`, `setStatus`, `getPreferredMajor`, `setPreferredMajor`, `getLevel`, `setLevel`, `getClosingDate`, `setClosingDate`, `matches`): expand filtering to support saved preferences per user, alphabetical default ordering, and composite filters (status + major + level + closing date).
- `Internship` (constructor, getters/setters, `addSlot`, `getSlots`, `getApplications`, `addApplication`, `toggleVisibility`, `isVisible`, `isFull`): enforce slot cap, status transitions (Pending → Approved/Rejected/Filled), auto-close when dates lapse, and keep visibility toggles synced with student views.
- `InternshipSlot#getSlotNumber`, `InternshipSlot#getAssignedStudent`, `InternshipSlot#assignStudent`: connect slot assignment to placement acceptance and ability to mark opportunities as filled.
- `Application` (constructor, `getStudent`, `getInternship`, `getStatus`, `setStatus`, `getTimestamp`, `isWithdrawalRequested`, `markSuccessful`, `markUnsuccessful`, `requestWithdrawal`): persist state changes, log timeline events, and expose status to menus even after posting visibility changes.
- `WithdrawalRequest#getApplication`, `WithdrawalRequest#getStudent`, `WithdrawalRequest#getReason`, `WithdrawalRequest#getStatus`, `WithdrawalRequest#approve`, `WithdrawalRequest#reject`: capture staff decisions, reasons, and timestamps for reporting/compliance.
- `AccountRequest#getRep`, `AccountRequest#getStatus`, `AccountRequest#setStatus`, `AccountRequest#getApprover`, `AccountRequest#setApprover`: expand to track submission times and rejection reasons so staff can manage approvals effectively.

## Phase 2 – User Loading & Registration Workflow

**UserManager**
- `UserManager#loadAllUsers`, `#loadStudents`, `#loadStaff`, `#loadCompanyRepresentatives`: harden CSV ingestion (validate ID formats, trim headers, guard against duplicates).
- `UserManager#getLastLoginMessage`: extend to return actionable hints for the CLI and future UI clients.
- `UserManager#approveRepresentative`: update account + request objects atomically, log approver/time, and expose rejection path.
- `UserManager#getPendingAccounts`: add pagination/filtering; ensure list stays consistent with persisted storage.

**App lifecycle**
- `App#App()` / `App#loadInitialUsers`: make the file paths configurable, handle missing files gracefully, and support refresh/reload commands.
- `App#start`: replace the placeholder loop with a structured menu router that can navigate to student/rep/staff portals and return cleanly.
- `App#promptLogin`: mask passwords, support password change/reset flows, and allow retries.
- `App#routeUser`: redirect to fully featured menus (student internship browsing, rep dashboard, staff console) once those are implemented.
- `App#handleRegistration`, `#registerStudent`, `#registerCompanyRep`, `#registerCareerCenterStaff`: add field validation, confirmation prompts, and persistence for new accounts. Ensure rep registration clearly communicates approval status.
- `App#readInt`: generalize into a reusable input utility (with default values, cancel paths, and error messaging).

## Phase 3 – Internship & Application Processing

**InternshipManager**
- `submitInternship`, `approveInternship`, `rejectInternship`, `filter`, `getInternships`: persist opportunities, enforce max-five-per-rep check, manage status transitions (Pending → Approved/Rejected/Filled), and expose filtered lists to students/company reps/career staff.

**ApplicationManager**
- `submitApplication`: integrate with notifications, enforce rule failures with friendly messages, and record the reasons when submissions fail.
- `updateStatus`: propagate status changes to students and reps, and auto-fill slots when approvals arrive.
- `enforceRules`: extend with additional requirements (visibility even after closing if student already applied, ability to view statuses, etc.) and extract messages describing which rule blocked the user.

**WithdrawalManager**
- `submitRequest`: capture reasons and ensure duplicate requests are prevented.
- `processRequest`: let staff approve or reject based on workload, update applications, and free up slots when withdrawals are accepted.

**Student / CompanyRep interactions**
- `Student#apply`, `#withdraw`, `#acceptPlacement`: call into ApplicationManager/WithdrawalManager, surface rule failures, and keep application list synced.
- `CompanyRep#createInternship`, `#viewApplications`, `#toggleVisibility`: use InternshipManager to create/update opportunities, enforce slot and visibility rules, and show application details with student info.

## Phase 4 – Staff Tools, Reporting, and UI Menus

**Career center workflows**
- `CareerCenterStaff#approveInternship`, `#rejectInternship`, `#approveRepAccount`, `#processWithdrawal`: replace direct field mutations with manager calls, ensuring audit logs and notifications fire.
- `WithdrawalManager#processRequest`, `ApplicationManager#updateStatus`: coordinate with staff decisions to keep records consistent.

**Reporting**
- `ReportGenerator#generateByStatus`, `#generateByMajor`, `#generateByLevel`, `#generateCompanySummary`: build comprehensive reports (counts, listings, filters by time window/company/major) and format them for CLI + future export.

**CLI menus**
- `App#showStudentMenu`: implement navigation for browsing/filtering internships, submitting applications, viewing statuses, requesting withdrawals, and accepting offers.
- `App#showRepMenu`: allow reps to submit postings, review applications, approve/reject candidates, toggle visibility, and monitor slot fulfillment.
- `App#showStaffMenu`: list pending rep accounts, internship submissions, withdrawal requests, and provide access to the reporting suite.

By following these phases in order—first strengthening the core entities, then user management, then application workflows, and finally the UI/reporting layers—you can iteratively turn the current skeleton into the fully featured internship management platform described in the requirements. Track progress by checking off each function as you implement its behavior.

## Additional Features

Once the core phases are complete, plan for these enhancements:

1. **Decision Notes Visibility**
   - Surface `AccountRequest` decision notes to both the affected company representative (when they attempt to log in or view their profile) and to relevant students if a decision affects their applications.

2. **Notification Framework**
   - Notify students when an application transitions to “pending acceptance.”
   - Notify career center staff when new withdrawal requests or representative account approvals await action.
   - Notify company reps when their accounts are approved or rejected, including decision notes.

3. **Withdrawal Comments**
   - Allow students to provide rich-text comments explaining why they want to withdraw.
   - Expose those comments to both career center staff (while reviewing requests) and the students themselves for future reference.

4. **Structured School/Major Selection**
   - Replace free-form major input with a school-first, major-second selection flow during student registration.
   - Represent schools and majors as enums (or a structured catalog) so filtering and internship creation can rely on consistent references.

5. **Multiple Preferred Majors Per Internship**
   - Allow company representatives to tag each internship with up to three preferred majors.
   - Update application filtering and eligibility checks to match on any of the listed majors.

Document these features alongside implementation details once they are in progress, so stakeholders understand their status and dependencies.
