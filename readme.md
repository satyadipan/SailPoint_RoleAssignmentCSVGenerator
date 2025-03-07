📌 GitHub README
# SailPoint Criteria Parser

🚀 **SailPoint Criteria Parser** is a Java-based tool that converts **human-readable role criteria** into the **JSON format** required by SailPoint.  
It reads role-based conditions from a **CSV file**, processes them, and writes the formatted JSON output back to another CSV file.

---

## 📜 Features

✅ **Reads role criteria from a CSV file**  
✅ **Parses complex conditions (`EQUALS`, `CONTAINS`, `NOT_EQUALS`)**  
✅ **Supports `AND` and `OR` operations with nested conditions**  
✅ **Handles missing quotes & numbers** (e.g., `jobcode equals 455786`)  
✅ **Preserves semicolon-enclosed values** (e.g., `;Finance Supply;`)  
✅ **Writes the JSON output to another CSV file**  
✅ **Fully compliant with SailPoint's `COMPLEX_CRITERIA` format**  

---

## 📦 Installation

1️⃣ **Clone the repository**
```bash
git clone https://github.com/yourusername/SailPoint-Criteria-Parser.git
cd SailPoint-Criteria-Parser
2️⃣ Compile the Java file

bash
Copy
Edit
javac -cp .:json-20210307.jar SailpointCSVParser.java
(Ensure you have the JSON library json-20210307.jar in your classpath.)

3️⃣ Run the application

java -cp .:json-20210307.jar SailpointCSVParser
📄 CSV Format
The input CSV should contain Role Name and Criteria.

🔹 Example Input (roles_input.csv)

RoleName,Criteria
Role1,("department" contains ("Finance" OR "IT")) AND (jobcode equals ";Finance Supply;")
Role2,("location" contains ("New York" OR "Los Angeles")) OR (status NOT_EQUALS "Inactive")
Role3,("role" equals "Manager") AND ("jobLevel" equals "Senior")
Role4,("employmentType" equals "Full-time") OR ("accessLevel" NOT_EQUALS "Restricted")
🔹 Example Output (roles_output.csv)
csv
Copy
Edit
RoleName,CriteriaJSON
Role1,{"children":[{"children":[{"key":{"property":"attribute.department","type":"IDENTITY"},"operation":"CONTAINS","value":"Finance"},{"key":{"property":"attribute.department","type":"IDENTITY"},"operation":"CONTAINS","value":"IT"}],"operation":"OR"},{"key":{"property":"attribute.jobcode","type":"IDENTITY"},"operation":"EQUALS","value":";Finance Supply;"}],"operation":"AND"}
Role2,{"children":[{"children":[{"key":{"property":"attribute.location","type":"IDENTITY"},"operation":"CONTAINS","value":"New York"},{"key":{"property":"attribute.location","type":"IDENTITY"},"operation":"CONTAINS","value":"Los Angeles"}],"operation":"OR"},{"key":{"property":"attribute.status","type":"IDENTITY"},"operation":"NOT_EQUALS","value":"Inactive"}],"operation":"OR"}
Role3,{"children":[{"key":{"property":"attribute.role","type":"IDENTITY"},"operation":"EQUALS","value":"Manager"},{"key":{"property":"attribute.jobLevel","type":"IDENTITY"},"operation":"EQUALS","value":"Senior"}],"operation":"AND"}
Role4,{"children":[{"key":{"property":"attribute.employmentType","type":"IDENTITY"},"operation":"EQUALS","value":"Full-time"},{"key":{"property":"attribute.accessLevel","type":"IDENTITY"},"operation":"NOT_EQUALS","value":"Restricted"}],"operation":"OR"}
🛠️ How It Works
1️⃣ Reads the CSV file
2️⃣ Uses regex to extract conditions (handles missing quotes, numbers, and semicolon-enclosed values)
3️⃣ Processes conditions (EQUALS, CONTAINS, NOT_EQUALS) and nested logic (AND/OR)
4️⃣ Generates JSON output
5️⃣ Writes the processed data back to a CSV file

⚡ Usage Example
🔹 Sample Condition
plaintext
Copy
Edit
("department" contains ("Finance" OR "IT")) AND (jobcode equals ";Finance Supply;")
🔹 Corresponding JSON Output
json
Copy
Edit
{
    "children": [
        {
            "children": [
                {
                    "key": {
                        "property": "attribute.department",
                        "type": "IDENTITY"
                    },
                    "operation": "CONTAINS",
                    "value": "Finance"
                },
                {
                    "key": {
                        "property": "attribute.department",
                        "type": "IDENTITY"
                    },
                    "operation": "CONTAINS",
                    "value": "IT"
                }
            ],
            "operation": "OR"
        },
        {
            "key": {
                "property": "attribute.jobcode",
                "type": "IDENTITY"
            },
            "operation": "EQUALS",
            "value": ";Finance Supply;"
        }
    ],
    "operation": "AND"
}
👨‍💻 Contributing
Fork the repository 🍴
Create a new feature branch 🔄
Commit your changes 💾
Push to the branch 🚀
Open a Pull Request ✨
📝 License
This project is open-source and available under the MIT License.
