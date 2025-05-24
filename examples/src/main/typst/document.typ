#let gitHash = sys.inputs.at("gitHash", default: "")
#let convertedImages = sys.inputs.at("common-converted-images")

= Test document #gitHash

#image(convertedImages + "/schweinestall.png")

#lorem(5000)
