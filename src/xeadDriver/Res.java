package xeadDriver;

/*
 * Copyright (c) 2015 WATANABE kozo <qyf05466@nifty.com>,
 * All rights reserved.
 *
 * This file is part of X-TEA Driver.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the XEAD Project nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

public class Res extends java.util.ListResourceBundle {
	private static final Object[][] contents = new String[][]{
		{ "Add", "Add" },
		{ "Asterisk", "* " },
		{ "ByteaFieldMessage1", "Browse Content" },
		{ "ByteaFieldMessage2", "Upload File" },
		{ "ByteaFieldMessage3", "Clear Content" },
		{ "ByteaFieldMessage4", "Processing Binary Data" },
		{ "ByteaFieldMessage5", "Choose option to process content data stored." },
		{ "ByteaFieldMessage6", "Specify File to Upload the Content" },
		{ "ByteaFieldMessage7", "Upload" },
		{ "ByteaFieldMessage8", "Uploading failed as file-extension is missing." },
		{ "ByteaFieldMessage9", "Uploading failed. Error:" },
		{ "ByteaFieldMessage10", "Browsing file failed as file type is unknown." },
		{ "ByteaFieldMessage11", "Browsing file failed. Error:" },
		{ "Calendar", "Calendar" },
		{ "CalendarComment", "Arrows=Select date, Shift+Right/Left Arrows=Shift month\nEnter=Set date, Ctrl+P=Type in, Ctrl+9=Set max-value" },
		{ "CalendarMessage1", "Input date value such as follows." },
		{ "CalendarMessage2", "Input date is invalid." },
		{ "Cancel", "Cancel" },
		{ "CheckBoxOnTableHeader", "Check to select all rows" },
		{ "CheckToContinue", "Check to Continue" },
		{ "CheckToDelete", "Check to Delete" },
		{ "ChrossCheck", "chross-check" },
		{ "Close", "Close" },
		{ "Colon", " : " },
		{ "Continue", "Continue" },
		{ "DBConnectMessage1", "Abort Session" },
		{ "DBConnectMessage2", "Re-Connect" },
		{ "DBConnectMessage3", "Connection to DB failed. Check status and choose option." },
		{ "DBConnectMessage4", "Check Connection" },
		{ "Descend", "(DESC)" },
		{ "DialogCheckReadContinue", "Continue Scanning" },
		{ "DialogCheckReadCountUnit", "Unit of Rows" },
		{ "DialogCheckReadMessage1", "Scanned rows are from " },
		{ "DialogCheckReadMessage2", " thru " },
		{ "DialogCheckReadMessage3", " and " },
		{ "DialogCheckReadMessage4", " of them are selected to list. " },
		{ "DialogCheckReadMessage5", "Specify action follows." },
		{ "DialogCheckReadNextRow", "Scanning from" },
		{ "DialogCheckReadRestart", "Restart Scanning" },
		{ "DialogCheckReadTitle", "Check Range of Scanning" },
		{ "Edit", "Edit" },
		{ "Exit", "Exit" },
		{ "ExitButton", "Exit" },
		{ "ExplosionAsRoot", "Explosion as root" },
		{ "FunctionError0", "Erros occured in Login-Script. Session was canceled." },
		{ "FunctionError1", "Script Error: The table '" },
		{ "FunctionError2", "' has the script '" },
		{ "FunctionError3", "' and it contains the invalid field definition '" },
		{ "FunctionError4", "...'." },
		{ "FunctionError5", "The process was canceled as an error occurred.\nRefer to the session log for details." },
		{ "FunctionError6", "The process was canceled as an SQL error occurred.\nRefer to the session log for details." },
		{ "FunctionError7", "The process was canceled as an error occurred in the script '" },
		{ "FunctionError8", "'.\nRefer to the session log for details." },
		{ "FunctionError9", "* The process was canceled as the denifition of the function '" },
		{ "FunctionError10", "' is missing." },
		{ "FunctionError11", " is not defined in the system." },
		{ "FunctionError12", "The process was canceled as an error occurred in the script of the function.\nRefer to the session log for details." },
		{ "FunctionError13", "* The function to process lines '" },
		{ "FunctionError14", "' does not exist." },
		{ "FunctionError15", "* It failed to launch the function." },
		{ "FunctionError16", "* Some value input required." },
		{ "FunctionError17", "* The function linked to the button '" },
		{ "FunctionError18", "' does not exist." },
		{ "FunctionError19", "* Another user has updated this record while you have read\nand tried to update. Otherwise, target is the read-only DB." },
		{ "FunctionError20", "* The process was canceled as it\nfailed to insert batch record(s)." },
		{ "FunctionError21", "* It failed to update record(s)." },
		{ "FunctionError22", "* The unique key of the record(s) to be inserted is duplicated with current records." },
		{ "FunctionError23", "Input valid data." },
		{ "FunctionError24", "Value of '" },
		{ "FunctionError25", "' is not defined in system kubun table." },
		{ "FunctionError26", "* It failed to update record for line " },
		{ "FunctionError27", " as its key value is modified." },
		{ "FunctionError28", "* The row of number " },
		{ "FunctionError29", " has duplicated value with the other record(s)." },
		{ "FunctionError30", "* It failed to process as the record was deleted for now." },
		{ "FunctionError31", "* It failed to process as the record with the same key value exists." },
		{ "FunctionError32", "* It's not allowed to edit key values." },
		{ "FunctionError33", "* Another user has updated this record while you have read\nand tried to delete. Otherwise, target is the read-only DB." },
		{ "FunctionError34", "* It failed to delete this record as data reffering to this exists." },
		{ "FunctionError35", "The table refering to " },
		{ "FunctionError36", " is missing." },
		{ "FunctionError37", "* The record with the key value specified is missing." },
		{ "FunctionError38", "* The record specified is not processable at the moment." },
		{ "FunctionError39", "Phrase formula '" },
		{ "FunctionError40", "' is invalid." },
		{ "FunctionError41", "* The HDR processing function '" },
		{ "FunctionError42", "' does not exist." },
		{ "FunctionError43", "* Unable to process rows as key value of the header table is altered." },
		{ "FunctionError44", "* Specify the row to be deleted." },
		{ "FunctionError45", " is not existing with the value specified." },
		{ "FunctionError46", " Default filter value failed to be set with specified keyword." },
		{ "FunctionError47", "Compulsory criteria '" },
		{ "FunctionError48", "' is missing." },
		{ "FunctionError49", "The value is out of valid range of account date." },
		{ "FunctionError50", "* Inserting is not permitted to the read-only DB." },
		//{ "FunctionError51", "* Function canceled as target record is not existing." },
		{ "FunctionError52", "* Function canceled as no columns are specified on Add-Row-List." },
		{ "FunctionError53", "" },
		{ "FunctionMessage1", "* Set criteria and push the button(or key Ctrl+L) to list data. Rows are listed in order of " },
		{ "FunctionMessage2", "* Use Arrow-Keys to select the line and enter. Rows will be sorted by the column values by clicking its heading. " },
		{ "FunctionMessage3", "* Use Arrow-Keys to select the line and enter to work with the data. Rows will be sorted by the column values by clicking its heading. " },
		{ "FunctionMessage4", "* No data found to be listed with the criteria." },
		{ "FunctionMessage5", "* Select row(s) to be processed and push '" },
		{ "FunctionMessage6", "' to continue. " },
		{ "FunctionMessage7", "* Modify field values as neccessary and push '" },
		{ "FunctionMessage8", "' to update. Enter-key is to check errors without updating." },
		{ "FunctionMessage9", "* Ready to process without errors. Push the button to update." },
		{ "FunctionMessage10", "Detail Inquiry of " },
		{ "FunctionMessage11", "" },
		{ "FunctionMessage12", "* Check data and push buttons for following actions." },
		{ "FunctionMessage13", "Adding New " },
		{ "FunctionMessage14", "" },
		{ "FunctionMessage15", "* Edit values and push the button to update table." },
		{ "FunctionMessage16", "* Ready to process without errors. Push the button to add." },
		{ "FunctionMessage17", "New " },
		{ "FunctionMessage18", " was added successfully. Continue to add?" },
		{ "FunctionMessage19", "* Edit values and push the button to add data." },
		{ "FunctionMessage20", "* The record was added successfully. But the function " },
		{ "FunctionMessage21", " was not executed as its definition missing." },
		{ "FunctionMessage22", "Editting " },
		{ "FunctionMessage23", "" },
		{ "FunctionMessage24", "* Edit values and push the button to update table." },
		{ "FunctionMessage25", "Copying " },
		{ "FunctionMessage26", "" },
		{ "FunctionMessage27", "Sure to " },
		{ "FunctionMessage28", " this record?" },
		{ "FunctionMessage29", "* Input key value(s) and push OK button." },
		{ "FunctionMessage30", "* Detail tabs and buttons are not defined." },
		{ "FunctionMessage31", "* No detail records found." },
		{ "FunctionMessage32", "* Select the row to be processed and enter." },
		{ "FunctionMessage33", "* It's canceled to add a line." },
		{ "FunctionMessage34", "* " },
		{ "FunctionMessage35", " records were not added because of duplicated key value(s)." },
		{ "FunctionMessage36", "Sure to delete the item number " },
		{ "FunctionMessage37", "?" },
		{ "FunctionMessage38", " has been started." },
		{ "FunctionMessage39", "* The line was not deleted." },
		{ "FunctionMessage40", " record(s) of " },
		{ "FunctionMessage41", " was updated." },
		{ "FunctionMessage42", " was updated and " },
		{ "FunctionMessage43", " was registered." },
		{ "FunctionMessage44", "The process will be executed at time specified. Push the start button to activate the timer." },
		{ "FunctionMessage45", "is the time to execute. Push the stop button or close this dialog to cancel timer." },
		{ "FunctionMessage46", "The timer is canceled." },
		{ "FunctionMessage47", "The process has started." },
		{ "FunctionMessage48", "The process has finished successfully." },
		{ "FunctionMessage49", "* You have no " },
		{ "FunctionMessage50", " to select." },
		{ "FunctionMessage51", "The process has ended abnormally." },
		{ "FunctionMessage52", "* Edit row(s) added." },
		{ "FunctionMessage53", "Web service is not available. Check Internet connection." },
		{ "FunctionMessage54", "Zip-No is invalid." },
		{ "FunctionMessage55", "Going to log out?             " },
		{ "FunctionMessage56", ". Use Shift-key to list in reverse order." },
		{ "FunctionMessage57", "* Rows will be sorted by the column values by clicking its heading. " },
		{ "FunctionMessage58", "Specify value of detail key field '" },
		{ "FunctionMessage59", "'." },
		{ "FunctionMessage60", "* No item appended." },
		{ "FunctionMessage61", "Specify file to be processed" },
		{ "FunctionMessage62", "The criterion of date-time will be ignored\nas the value '" },
		{ "FunctionMessage63", "' is invalid." },
		{ "Iconify", "Iconify" },
		{ "ImageFileMessage1", "Click to browse the image." },
		{ "ImageFileNotFound1", "<html>The image file specified<br>" },
		{ "ImageFileNotFound2", "<br>is not found in folders." },
		{ "ImageFileShown", "Click to browse in original size." },
		{ "Implosion", "Implosion" },
		{ "ImplosionAsRoot", "Implosion as root" },
		{ "InputImageFileName", "Input Image File Name" },
		{ "JavaVersionError1", "Launching failed as Java version is " },
		{ "JavaVersionError2", ". Required version is 1.7 - 1.8" },
		{ "LineNumber1", "(Line " },
		{ "LineNumber2", ") : " },
		{ "LogIn", "Log in" },
		{ "LogInComment", "Input the ID and password to log in." },
		{ "LogInError1", "This ID is expired now." },
		{ "LogInError2", "The ID or password is not valid." },
		{ "LogInError3", "Logging-in is currently restricted. Call the system administrator." },
		{ "LogInError4", "Logging-in failed as system variant of LOGIN_PERMITTED is not registered.\nCall the system administrator." },
		{ "LogInError5", "Logging-in failed as user variant of KBCALENDAR is not registered.\nCall the system administrator." },
		{ "LogOut", "Log out" },
		{ "MinusError", "Minus value is not apllied for the field." },
		{ "Modify", "Modify" },
		{ "ModifyPassword", "Modify Password" },
		{ "ModifyPasswordError1", "New password is invalid. Its\nlength must be more than 4." },
		{ "ModifyPasswordError2", "It failed to modify password.\nCurrent passoword may be invalid." },
		{ "ModifyPasswordError3", "Values of new password do not match." },
		{ "Next", "Next" },
		{ "No", "No" },
		{ "NumberFormatError", "Decimal point is invalid." },
		{ "Password", "Password" },
		{ "PasswordCurrent", "Cur. Password" },
		{ "PasswordModified", "* Password was modified successfully." },
		{ "PasswordNew", "New Password" },
		{ "ReferCheckerMessage0", "* " },
		{ "ReferCheckerMessage1", " (The line no " },
		{ "ReferCheckerMessage2", " ) - " },
		{ "ReferCheckerMessage3", "It's not allowed to insert this record as inconsistency will occur with records of '" },
		{ "ReferCheckerMessage4", "'. Refer to session log." },
		{ "ReferCheckerMessage5", "It's not allowed to update this record as inconsistency will occur with records of '" },
		{ "ReferCheckerMessage6", "'. Refer to session log." },
		{ "ReferCheckerMessage7", "It's not allowed to delete this record as valid records of '" },
		{ "ReferCheckerMessage8", "' referring to this exists. Refer to session log." },
		{ "ReferCheckerMessage9", "* Process is not permitted as inconsistency occurs with the record(" },
		{ "ReferCheckerMessage10", ")." },
		//{ "Refresh", "Refresh" },
		{ "ReturnMessage00", "* Data is processed successfully." },
		{ "ReturnMessage01", "* Processing is canceled." },
		{ "ReturnMessage10", "* Data is registered successfully." },
		{ "ReturnMessage11", "* Registration is canceled." },
		{ "ReturnMessage20", "* Data is updated successfully." },
		{ "ReturnMessage21", "* Update is canceled." },
		{ "ReturnMessage30", "* Data is deleted successfully." },
		{ "ReturnMessage31", "* Delete is canceled." },
		{ "ReturnMessage99", "* Processing is aborted. Refer to the logs for the details." },
		{ "Search", "List" },
		{ "Sel", "Sel" },
		{ "SessionError1", "Session was not started as the system definition file is not specified." },
		{ "SessionError2", "Session was not started as parsing failed with the XML file '" },
		{ "SessionError3", "'." },
		{ "SessionError4", "It failed to launch the system '" },
		{ "SessionError5", "' as the database service is not activated." },
		{ "SessionError6", "It failed to connect to the database '" },
		{ "SessionError7", "'.\n" },
		{ "SessionError8", "Warnings : Print font '" },
		{ "SessionError9", "' is invalid. Default PDF font(Times-Roman) will be used instead." },
		{ "SessionError10", "You can specify URL of 'Wellcome Page' for this menu using X-TEA Editor. Note that the page must be a simple HTML site without CSS nor scripts." },
		{ "SessionError11", "It failed to open the page '" },
		{ "SessionError12", "'." },
		{ "SessionError13", "It failed to get a new number as the numbering key '" },
		{ "SessionError14", "'\ndoes not exist in the system numbering table." },
		{ "SessionError15", "The function is not defined in the system." },
		{ "SessionError16", "HELP page is not specified for menu of '" },
		{ "SessionError17", "'." },
		{ "SessionError18", "It failed to access to HELP page for menu of '" },
		{ "SessionError19", "'.\n" },
		{ "SessionError20", "This function is rejected to be called as\nreccursive calls are prohibitted any more." },
		{ "SessionError21", "Session was not started as the content file with name '" },
		{ "SessionError22", "' does not exist." },
		{ "SessionMessage1", "* Use Arrow-Keys to select the menu option and enter to execute. Use F12 to change log-in password." },
		{ "SessionMessage2", "* Use Arrow-Keys to select the menu option and enter to execute. Use F1 to browse manuals, F12 to change log-in password." },
		{ "SplashMessage0", "Launching X-TEA Driver..." },
		{ "SplashMessage1", "Parsing system definitions..." },
		{ "SplashMessage2", "Loading the session..." },
		{ "SplashMessage3", "Loading cross-checkers..." },
		{ "SplashMessage4", "Loading functions..." },
		{ "TimerCondition", "Condition" },
		{ "TimerError", "Process was canceled as time value specified is invalid." },
		{ "TimerStart", "Start" },
		{ "TimerStop", "Stop" },
		{ "TimerTime", "Time to Run" },
		{ "TimerRepeat", "Repeat in time" },
		{ "TimerRunNow", "Run right now" },
		{ "TimerRunOffDay", "Run on day off" },
		{ "Total", "Total" },
		{ "Update", "Update" },
		{ "URLFont", "Times New Roman" },
		{ "UserID", "User ID" },
		{ "UserName", "User Name" },
		{ "XLSComment1", "* Data was output into EXCEL sheet." },
		{ "XLSComment2", "* These are subset with criteria�F" },
		{ "XLSErrorMessage", "IOException of EXCEL data. Refer to session log for details." },
		{ "Yes", "Yes" }
	};

	public Object[][] getContents() {
		return contents;
	}
}