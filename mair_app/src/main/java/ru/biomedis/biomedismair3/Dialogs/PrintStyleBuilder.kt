package ru.biomedis.biomedismair3.Dialogs

class PrintStyleBuilder {
    companion object {
        @JvmStatic
        fun build(): String {
            return """
            <style>
             
            @media print {
            	html, body{
            		height: 297mm;
            		width: 210mm;
                    margin:0mm;
                    padding:0mm;
            	}
            body {
                 font-family: Times, 'Times New Roman', serif; 
                }
             
             table{
             margin: 0mm;
             padding:0mm;
             width:100%;
             }
            }
            
            @page {
            	margin: 5mm 10mm 5mm 20mm;
            }            
            </style> 
        """.trimIndent()
        }
    }
}
