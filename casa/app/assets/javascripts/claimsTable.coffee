window.constructClaimsTable = (claimsList, claimsListSize) ->


    claimsTable = $('#main').find("table")

    table = $("#claimsTable")

    caption = $('<caption/>')
    $(caption).append("List of claims")


    $(table).append(caption)

    thead = $('<thead/>')
    trHeader = $('<tr/>')
    thBlank = $('<th/>')
    th1 = $('<th/>' )
    th2 = $('<th/>').attr('class', 'view' )
    th3 = $('<th/>').attr('class', 'ni' ).append("NI Number")
    th4 = $('<th/>').attr('class', 'view' ).append("Claim type")
    th5 = $('<th/>').attr('class', 'view' ).append("Submit Date")
    th6 = $('<th/>').attr('class', 'view' ).append("Status")

    thIdLink = $('<a/>' ).attr('href','#').attr('id', 'thId').attr('class', 'thId' ).attr('style', 'color:white').append('Transaction Id')
    thNameLink = $('<a/>' ).attr('href','#').attr('id', 'thNameId').attr('class', 'thName' ).attr('style', 'color:white').append('Name')

    $(thIdLink).on "click", ->
      sort('thIdLink')

    $(thIdLink).on "hover", ->
      $(thIdLink).color = '#fff'

    $(thNameLink).on "click", ->
      sort('thNameLink')


    $(th1).append(thIdLink)
    $(th2).append(thNameLink)

    $(trHeader).append(thBlank).append(th1).append(th2).append(th3).append(th4).append(th5).append(th6)

    $(thead ).append(trHeader)

    table.append(thead)

    tBody = $('<tbody/>')

    tr = []
    completedCheckboxes = []

    for i in [0..claimsList.length-1] by 1
      claim = claimsList[i]
      trId = claim.transactionId
      trIdRow = "row_"+trId;

      tr[i] = $('<tr/>' ).attr('id', trIdRow)

      td1 = $('<td/>').attr('class', 'transactionId' )
      td2 = $('<td/>').attr('class', 'name' )
      td3 = $('<td/>').attr('class', 'ni' )
      td4 = $('<td/>').attr('class', 'view' )
      td5 = $('<td/>').attr('class', 'view' )
      td6 = $('<td/>').attr('class', 'status' )

      trIdHref = "/render/"+trId
      trIdLink = $('<a/>' ).attr('href', trIdHref ).attr('target',trId)

      $(trIdLink).append( claim.transactionId )
      $(td1).append( trIdLink )
      $(td2).append( claim.surname + " " + claim.forename)
      $(td3).append( claim.nino )
      $(td4).append( claim.claimType )

      theDate = new Date(claim.claimDateTime)

      $(td5).append( theDate )
      $(td6).append( claim.status )


      tdCheck = $('<td/>')

      labelTrId = $('<label/>')
      $(labelTrId).attr('for',trId)
      $(labelTrId).append('Claim')

      inputTrIdCheck = $('<input/>')
      $(inputTrIdCheck).attr('type','checkbox')
      inputTrIdCheck.attr('name', completedCheckboxes[i])
      inputTrIdCheck.attr('id', trId)
      inputTrIdCheck.attr('value', trId)

      if (claim.status == "viewed")
        tdCheck.append(labelTrId)
        tdCheck.append(inputTrIdCheck)

      $(tr[i]).append(tdCheck).append(td1).append(td2).append(td3).append(td4).append(td5).append(td6)

      $(tBody).append(tr[i])

      $(table).append(tBody)

sort = (elem) ->
    sorted = elem
    table = $("#claimsTable")
    rows = table.find('tr:gt(0)').toArray().sort(comparer($(elem).index()))
    elem.asc = !elem.asc
    if (!elem.asc)
      rows = rows.reverse()
    for i in [0..rows.length] by 1
      table.append(rows[i])

comparer = (index) ->
  sortRows = (a,b) ->
    valA = getCellValue(a, index)
    valB = getCellValue(b, index)
    $.isNumeric(valA) && $.isNumeric(valB) ? valA - valB : valA.localeCompare(valB)

getCellValue = (row, index) ->
  $(row).children('td').eq(index).html()

formatDate = (date) -> date.toLocaleDateString()


