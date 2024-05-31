<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="description" content="邮箱验证码">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <base target="_blank">
    <style>::-webkit-scrollbar{ display: none; }</style>
</head>
<body tabindex="0">
<div style="background-color: #ECECEC; padding: 25px;">
    <div style="margin: 0 auto; text-align: left; position: relative; border-radius: 5px; border-collapse: collapse; box-shadow: rgb(153, 153, 153) 0px 0px 5px; background: #fff; font-family: 微软雅黑, 黑体, sans-serif; font-size: 14px; line-height: 1.5;">
        <div style="height: 29px; line-height: 25px; padding: 15px 30px; border-bottom-width: 1px; border-bottom-style: solid; border-bottom-color: #307AF2; background: #00308f; border-radius: 5px 5px 0 0;">
            <div style="font-size: 24px; font-weight: bolder; color: #fff; display: inline-flex; align-items: center;">
                <a href="${siteUrl}">
                    <img src="https://admin.continew.top/logo.svg" alt="${siteTitle}" style="vertical-align: middle;">
                </a>
                <a href="${siteUrl}" style="margin-left: 4px; text-decoration: none; color: #fff;">${siteTitle}</a>
            </div>
        </div>
        <div style="word-break: break-word;">
            <div style="border-radius: 5px; padding: 25px 30px 11px; background-color: #fff; opacity: 0.8;">
                <h2 style="margin: 5px 0; font-size: 18px; line-height: 22px; color: #333;">亲爱的用户：</h2>
                <p>
                    您好！感谢您使用 <a href="${siteUrl}" style="color: #333;">${siteTitle}</a>，本次请求的验证码为：<span style="font-size: 16px; color: #ff8c00;">${captcha}</span>，请在 ${expiration} 分钟内使用此验证码完成验证。
                </p>
                <br>
                <h2 style="margin: 5px 0; font-size: 18px; line-height: 22px; color: #333;">Dear user:</h2>
                <p>
                    Hello! Thanks for using ${siteTitle}, The verification code for this request is:&nbsp;<span style="font-size: 16px; color: #ff8c00;">${captcha}</span>, please use this verification code to complete the verification within ${expiration} minutes.
                </p>
                <div style="width: 100%; margin: 0 auto;">
                    <div style="padding: 10px 10px 0; border-top: 1px solid #ccc; color: #747474; margin-bottom: 20px; line-height: 1.3em; font-size: 12px;">
                        <p>
                            若非本人操作，请忽略此邮件。此邮件由系统自动发送，请勿直接回复该邮件。<br>
                            Please ignore this email if not by yourself. This email is sent automatically by the system, please do not reply to this email directly.
                        </p>
                        <p>${siteCopyright}</p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>