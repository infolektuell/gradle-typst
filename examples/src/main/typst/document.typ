#let gitHash = sys.inputs.at("gitHash", default: "")
#let info = yaml("../data/info.yml")
#set text(font: "Dejavu Sans")
= #info.title #gitHash

#lorem(5000)
