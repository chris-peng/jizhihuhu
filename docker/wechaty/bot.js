import { WechatyBuilder, ScanStatus } from 'wechaty'
import request from 'request'
import qrcodeGenerate from 'qrcode-terminal'

var sw = true;

const wechaty = WechatyBuilder.build({name: "myWechatyBot"}) // get a Wechaty instance
wechaty
  .on('scan', (qrcode, status) => {
	if (status === ScanStatus.Waiting || status === ScanStatus.Timeout) {
                //console.log(`Scan QR Code to login: ${status}\nhttps://wechaty.js.org/qrcode/${encodeURIComponent(qrcode)}`)
                qrcodeGenerate.generate(qrcode, {small: true})
        }
  })
  .on('login', user => {
		console.log(`User ${user} logged in`)
		wechaty.Contact.find({name: process.env.test_user}).then((testContact) => {
			setInterval(() => testContact.say('hello, 机智糊糊'), 24 * 3600 * 1000)
		})
  })
  .on('room-join', (room, inviteeList, inviter) => {
		console.log('room join', room.topic())
	  for(let i = 0; i < inviteeList.length; i++) {
		  if(inviteeList[i].self()){
			  room.say(process.env.introduce);
			  return;
		  }
	  }
  })
  .on('message', message => {
		if(message.type() == 7 && message.talker().name() == process.env.admin && message.to() != null && message.to().self()) {
			if(message.text() == '开') {
				sw = true;
				return;
			} else if(message.text() == '关') {
				sw = false;
				return;
			}
		}
		if(!sw) {
			return;
		}
		if(message.talker().self() || message.talker().name() == '微信团队'){
			return;
		}
		//console.log(`Message: ${message.type()}, ${message.text()}, ${message.self()}, ${message.room()}`)
		message.mentionSelf().then((mentionSelf) => {
			//console.log('mentionSelf', mentionSelf);
			if(message.room() != null && !mentionSelf) {
				return;
			}
			request.post({
				url: 'http://172.22.0.2:8890/wechaty/ai',
				json: true,
				body: {
					type: message.type(),
					text: message.text(),
					talkerName: message.talker().name(),
					talkerId: message.talker().id,
					toName: message.to() != null ? message.to().name() : null,
					toId: message.to() != null ? message.to().id : null,
					roomId: message.room() != null ? message.room().id : null,
					isSelf: message.self(),
					mentionSelf: mentionSelf
				}
			}, (err,res,aiResponse) => {
				if(err) {
				  console.log('err', err);
				  return;
				}
				if(aiResponse.success && aiResponse.message != '') {
					if(message.room() != null){
						message.room().say(aiResponse.message);
					} else {
						message.talker().say(aiResponse.message);
					}
				} else {
					console.log('request ai error: ' + aiResponse.error);
				}
			})
		});
})
wechaty.start()
